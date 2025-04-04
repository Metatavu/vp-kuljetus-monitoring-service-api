package fi.metatavu.vp.monitoring.monitors

import fi.metatavu.vp.api.model.ThermalMonitor
import fi.metatavu.vp.api.model.ThermalMonitorSchedulePeriod
import fi.metatavu.vp.api.model.ThermalMonitorStatus
import fi.metatavu.vp.api.model.ThermalMonitorType
import fi.metatavu.vp.monitoring.monitors.schedules.ThermalMonitorSchedulePeriodController
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

    @Inject
    lateinit var thermalMonitorSchedulePeriodController: ThermalMonitorSchedulePeriodController

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
            activeTo = thermalMonitor.activeTo,
            monitorType = thermalMonitor.monitorType.toString()
        )

        thermalMonitor.thermometerIds.forEach {
            monitorThermometerController.create(
                thermometerId = it,
                thermalMonitorEntity = monitor,
                creatorId = creatorId
            )
        }

        if (thermalMonitor.monitorType == ThermalMonitorType.SCHEDULED) {
            thermalMonitor.schedule!!.forEach { schedule ->
                thermalMonitorSchedulePeriodController.create(
                    thermalMonitor = monitor,
                    startWeekDay = schedule.start.weekday,
                    startHour = schedule.start.hour,
                    startMinute = schedule.start.minute,
                    endWeekDay = schedule.end.weekday,
                    endHour = schedule.end.hour,
                    endMinute = schedule.end.minute
                )
            }
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

        thermalMonitorSchedulePeriodController.list(thermalMonitor = thermalMonitorEntity).forEach {
            thermalMonitorSchedulePeriodController.delete(it)
        }

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
        monitorType: ThermalMonitorType? = null,
        first: Int? = null,
        max: Int? = null
    ): List<ThermalMonitorEntity> {
        return thermalMonitorRepository.list(
            status = status,
            activeAfter = activeAfter,
            activeBefore = activeBefore,
            toBeActivatedBefore = toBeActivatedBefore,
            monitorType = monitorType?.toString(),
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

        if (thermalMonitor.monitorType == ThermalMonitorType.SCHEDULED) {
            handleSchedulePeriodsUpdate(
                thermalMonitorEntity = thermalMonitorEntity,
                schedulePeriods = thermalMonitor.schedule!!
            )
        }

        handleThermometersUpdate(
            thermalMonitor = thermalMonitor,
            thermalMonitorEntity = thermalMonitorEntity,
            modifier = modifier,
            deleteUnusedThermometersPermanently = deleteUnusedThermometersPermanently
        )

        return thermalMonitorRepository.updateFromRest(thermalMonitorEntity, thermalMonitor, modifier)
    }

    /**
     * Resolve statuses for singular monitors based on individual monitor's settings
     *
     *  - Set status to ACTIVE if monitor status is PENDING and monitor activeFrom is before now
     *  - Set status to FINISHED if monitor status is ACTIVE and monitor activeTo is before now
     */
    suspend fun resolveSingularMonitorStatuses() {
        list(
            status = ThermalMonitorStatus.PENDING,
            monitorType = ThermalMonitorType.SINGULAR,
            toBeActivatedBefore = OffsetDateTime.now()
        ).forEach { thermalMonitorRepository.activateThermalMonitor(it) }

        list(
            status = ThermalMonitorStatus.ACTIVE,
            monitorType = ThermalMonitorType.SINGULAR,
            activeBefore = OffsetDateTime.now()
        ).forEach { thermalMonitorRepository.finishThermalMonitor(it) }
    }

    /**
     * Resolve statuses for scheduled monitors based on individual monitor's settings
     *
     *  - Set status to ACTIVE if monitor status is INACTIVE and monitor has a schedule period that is active right now
     *  - Set status to INACTIVE if monitor status is ACTIVE and monitor does not have any active schedule periods
     */
    suspend fun resolveScheduledMonitorStatuses() {
        thermalMonitorSchedulePeriodController.list(
            activeAt = OffsetDateTime.now(),
            thermalMonitorStatus = ThermalMonitorStatus.INACTIVE
        ).forEach { schedulePeriod ->
            val thermalMonitor = schedulePeriod.thermalMonitor
            thermalMonitorRepository.activateThermalMonitor(thermalMonitor)
        }

        thermalMonitorRepository.listScheduledActiveMonitorsWithoutActiveSchedules(
            currentTime = OffsetDateTime.now()
        ).forEach { thermalMonitor ->
            thermalMonitorRepository.deactivateThermalMonitor(thermalMonitor)
        }
    }

    /**
     * Creates and deletes schedule periods based on the updated schedule list on a monitor
     *
     * @param thermalMonitorEntity
     * @param schedulePeriods
     */
    private suspend fun handleSchedulePeriodsUpdate(
        thermalMonitorEntity: ThermalMonitorEntity,
        schedulePeriods: List<ThermalMonitorSchedulePeriod>
    ) {
        thermalMonitorSchedulePeriodController.list(thermalMonitor = thermalMonitorEntity).forEach {
            thermalMonitorSchedulePeriodController.delete(it)
        }

        schedulePeriods.forEach {
            thermalMonitorSchedulePeriodController.create(
                thermalMonitor = thermalMonitorEntity,
                startWeekDay = it.start.weekday,
                startHour = it.start.hour,
                startMinute = it.start.minute,
                endWeekDay = it.end.weekday,
                endHour = it.end.hour,
                endMinute = it.end.minute
            )
        }
    }

    /**
     * Creates and archives (or deletes in testing environments) thermometers based on the updated thermometer list on a monitor
     *
     * @param thermalMonitor
     * @param thermalMonitorEntity
     * @param modifier
     * @param deleteUnusedThermometersPermanently
     */
    private suspend fun handleThermometersUpdate(
        thermalMonitor: ThermalMonitor,
        thermalMonitorEntity: ThermalMonitorEntity,
        modifier: UUID,
        deleteUnusedThermometersPermanently: Boolean = false
    ) {
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
    }
}