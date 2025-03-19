package fi.metatavu.vp.monitoring.monitors

import fi.metatavu.vp.api.model.ThermalMonitor
import fi.metatavu.vp.api.model.ThermalMonitorStatus
import fi.metatavu.vp.monitoring.monitors.thermometers.MonitorThermometerController
import fi.metatavu.vp.monitoring.policies.PagingPolicyController
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.OffsetDateTime
import java.util.UUID

@ApplicationScoped
class ThermalMonitorController {
    @Inject
    lateinit var thermalMonitorRepository: ThermalMonitorRepository

    @Inject
    lateinit var monitorThermometerController: MonitorThermometerController

    @Inject
    lateinit var pagingPolicyController: PagingPolicyController

    /**
     * Create a thermal monitor to monitor for incidents
     *
     * @param thermalMonitor
     * @param creatorId
     */
    suspend fun create(thermalMonitor: ThermalMonitor, creatorId: UUID): ThermalMonitorEntity {
        val monitor = thermalMonitorRepository.create(
            name = thermalMonitor.name,
            status = thermalMonitor.status.toString(),
            creatorId = creatorId,
            thresholdLow = thermalMonitor.lowerThresholdTemperature,
            thresholdHigh = thermalMonitor.upperThresholdTemperature,
            activeFrom = thermalMonitor.activeFrom,
            activeTo = thermalMonitor.activeTo
        )

        thermalMonitor.thermometerIds.forEach {
            monitorThermometerController.create(
                thermometerId = it,
                thermalMonitorEntity = monitor,
                creatorId = creatorId
            )
        }

        return monitor
    }

    /**
     * Find thermal monitor
     *
     * @param id monitor id
     */
    suspend fun find(id: UUID): ThermalMonitorEntity? {
        return thermalMonitorRepository.findByIdSuspending(id)
    }

    /**
     * Delete thermal monitor
     *
     * @param thermalMonitorEntity
     */
    suspend fun delete(thermalMonitorEntity: ThermalMonitorEntity) {
        monitorThermometerController.listThermometers(
            thermalMonitorEntity = thermalMonitorEntity,
            thermometerId = null,
            onlyActive = false,
            includeArchived = true
        ).forEach {
            monitorThermometerController.delete(it)
        }

        pagingPolicyController.deletePoliciesByMonitor(thermalMonitorEntity)

        thermalMonitorRepository.deleteSuspending(thermalMonitorEntity)
    }

    /**
     * List thermal monitors
     *
     * @param status
     * @param activeBefore
     * @param activeAfter
     * @param toBeActivatedBefore
     * @param first
     * @param max
     */
    suspend fun list(
        status: ThermalMonitorStatus? = null,
        activeBefore: OffsetDateTime? = null,
        activeAfter: OffsetDateTime? = null,
        toBeActivatedBefore: OffsetDateTime? = null,
        first: Int? = null,
        max: Int? = null
    ): List<ThermalMonitorEntity> {
        return thermalMonitorRepository.list(
            status = status,
            activeAfter = activeAfter,
            activeBefore = activeBefore,
            toBeActivatedBefore = toBeActivatedBefore,
            first = first,
            max = max
        ).first
    }

    /**
     * Update thermal monitor
     *
     * @param thermalMonitor updated data
     * @param thermalMonitorEntity existing entity
     * @param modifier modifier
     * @param deleteUnusedThermometersPermanently delete thermometers that are not in the updated list
     */
    suspend fun updateFromRest(
        thermalMonitor: ThermalMonitor,
        thermalMonitorEntity: ThermalMonitorEntity,
        modifier: UUID,
        deleteUnusedThermometersPermanently: Boolean = false): ThermalMonitorEntity {
        val existingThermometers = monitorThermometerController.listThermometers(
            thermalMonitorEntity = thermalMonitorEntity,
            thermometerId = null,
            onlyActive = false
        )

        existingThermometers.forEach {
            if (!thermalMonitor.thermometerIds.contains(it.thermometerId)) {

                if (deleteUnusedThermometersPermanently) {
                    monitorThermometerController.delete(it)
                } else {
                    monitorThermometerController.archiveThermometer(it, modifier)
                }

            }
        }

        val existingIds = existingThermometers.map { it.thermometerId }
        thermalMonitor.thermometerIds.distinct().forEach {
            if (!existingIds.contains(it)) {
                monitorThermometerController.create(
                    it,
                    thermalMonitorEntity,
                    modifier
                )
            }
        }

        return thermalMonitorRepository.updateFromRest(thermalMonitorEntity, thermalMonitor, modifier)
    }

    /**
     * Resolve statuses for monitors based on individual monitor's settings
     *
     *  - Set status to ACTIVE if monitor status is PENDING and monitor activeFrom is before now
     *  - Set status to FINISHED if monitor status is ACTIVE and monitor activeTo is before now
     */
    suspend fun resolveMonitorStatuses() {
        list(
            status = ThermalMonitorStatus.PENDING,
            toBeActivatedBefore = OffsetDateTime.now()
        ).forEach { thermalMonitorRepository.activateThermalMonitor(it) }

        list(
            status = ThermalMonitorStatus.ACTIVE,
            activeBefore = OffsetDateTime.now()
        ).forEach { thermalMonitorRepository.finishThermalMonitor(it) }
    }
}