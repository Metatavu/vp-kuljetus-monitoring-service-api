package fi.metatavu.vp.monitoring.monitors

import fi.metatavu.vp.api.model.ThermalMonitor
import fi.metatavu.vp.api.model.ThermalMonitorStatus
import fi.metatavu.vp.monitoring.monitors.thermometers.MonitorThermometerController
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
        monitorThermometerController.listThermometers(thermalMonitorEntity = thermalMonitorEntity, thermometerId = null).forEach {
            monitorThermometerController.delete(it)
        }

        thermalMonitorRepository.deleteSuspending(thermalMonitorEntity)
    }

    /**
     * List thermal monitors
     *
     * @param status
     * @param activeBefore
     * @param activeAfter
     * @param first
     * @param max
     */
    suspend fun list(status: ThermalMonitorStatus?, activeBefore: OffsetDateTime?, activeAfter: OffsetDateTime?, first: Int?, max: Int?): List<ThermalMonitorEntity> {
        return thermalMonitorRepository.list(
            status = status,
            activeAfter = activeAfter,
            activeBefore = activeBefore,
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
     */
    suspend fun update(thermalMonitor: ThermalMonitor, thermalMonitorEntity: ThermalMonitorEntity, modifier: UUID): ThermalMonitorEntity {
        val existingThermometers = monitorThermometerController.listThermometers(thermalMonitorEntity = thermalMonitorEntity, thermometerId = null)

        existingThermometers.forEach {
            if (!thermalMonitor.thermometerIds.contains(it.thermometerId)) {
                monitorThermometerController.delete(it)
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

        return thermalMonitorRepository.update(thermalMonitorEntity, thermalMonitor, modifier)
    }
}