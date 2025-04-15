package fi.metatavu.vp.monitoring.monitors

import fi.metatavu.vp.api.model.ThermalMonitor
import fi.metatavu.vp.api.model.ThermalMonitorStatus
import fi.metatavu.vp.monitoring.persistence.AbstractRepository
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Database operations for thermal monitors
 */
@ApplicationScoped
class ThermalMonitorRepository: AbstractRepository<ThermalMonitorEntity, UUID>() {
    /**
     * Save thermal monitor to the database
     *
     * @param name
     * @param status
     * @param creatorId
     * @param thresholdLow
     * @param thresholdHigh
     * @param activeFrom
     * @param activeTo
     * @param monitorType
     */
    suspend fun create(
        name: String,
        status: String,
        creatorId: UUID,
        thresholdLow: Float?,
        thresholdHigh: Float?,
        activeFrom: OffsetDateTime?,
        activeTo: OffsetDateTime?,
        monitorType: String
    ): ThermalMonitorEntity {
        val monitor = ThermalMonitorEntity()
        monitor.id = UUID.randomUUID()
        monitor.name = name
        monitor.status = status
        monitor.creatorId = creatorId
        monitor.lastModifierId = creatorId
        monitor.thresholdLow = thresholdLow
        monitor.thresholdHigh = thresholdHigh
        monitor.activeFrom = activeFrom
        monitor.activeTo = activeTo
        monitor.monitorType = monitorType
        return persistSuspending(monitor)
    }

    /**
     * List thermal monitors
     *
     * @param status
     * @param activeAfter
     * @param activeBefore
     * @param toBeActivatedBefore
     * @param monitorType
     * @param first
     * @param max
     */
    suspend fun list(
        status: ThermalMonitorStatus?,
        activeAfter: OffsetDateTime?,
        activeBefore: OffsetDateTime?,
        toBeActivatedBefore: OffsetDateTime?,
        monitorType: String?,
        first: Int?,
        max: Int?
    ): Pair<List<ThermalMonitorEntity>, Long> {
        val queryBuilder = StringBuilder()
        val parameters = Parameters()

        if (status != null) {
            addCondition(queryBuilder, "status = :status")
            parameters.and("status", status.toString())
        }

        if (activeAfter != null) {
            addCondition(queryBuilder, "activeFrom > :activeAfter")
            parameters.and("activeAfter", activeAfter)
        }

        if (activeBefore != null) {
            addCondition(queryBuilder, "activeTo < :activeBefore")
            parameters.and("activeBefore", activeBefore)
        }

        if (toBeActivatedBefore != null)  {
            addCondition(queryBuilder, "activeFrom < :toBeActivatedBefore")
            parameters.and("toBeActivatedBefore", toBeActivatedBefore)
        }

        if (monitorType != null) {
            addCondition(queryBuilder, "monitorType = :monitorType")
            parameters.and("monitorType", monitorType)
        }

        return applyFirstMaxToQuery(find(queryBuilder.toString(), parameters), firstIndex = first, maxResults = max)
    }

    /**
     * Update thermal monitor
     *
     * @param thermalMonitorEntity thermal monitor entity
     * @param thermalMonitor updated data
     * @param modifier modifier
     */
    suspend fun updateFromRest(thermalMonitorEntity: ThermalMonitorEntity, thermalMonitor: ThermalMonitor, modifier: UUID): ThermalMonitorEntity {
        val updated = thermalMonitorEntity
        updated.name = thermalMonitor.name
        updated.status = thermalMonitor.status.toString()
        updated.lastModifierId = modifier
        updated.thresholdLow = thermalMonitor.lowerThresholdTemperature
        updated.thresholdHigh = thermalMonitor.upperThresholdTemperature
        updated.activeFrom = thermalMonitor.activeFrom
        updated.activeTo = thermalMonitor.activeTo
        updated.monitorType = thermalMonitor.monitorType.toString()

        return persistSuspending(updated)
    }

    /**
     * Activate a thermal monitor
     * This is used by the cron job that changes monitor statuses based on monitor settings
     *
     * @param thermalMonitorEntity
     */
    suspend fun activateThermalMonitor(thermalMonitorEntity: ThermalMonitorEntity): ThermalMonitorEntity {
        thermalMonitorEntity.status = ThermalMonitorStatus.ACTIVE.toString()
        return persistSuspending(thermalMonitorEntity)
    }

    /**
     * Finish a thermal monitor
     * This is used by the cron job that changes monitor statuses based on monitor settings
     *
     * @param thermalMonitorEntity
     */
    suspend fun finishThermalMonitor(thermalMonitorEntity: ThermalMonitorEntity): ThermalMonitorEntity {
        thermalMonitorEntity.status = ThermalMonitorStatus.FINISHED.toString()
        return persistSuspending(thermalMonitorEntity)
    }

    /**
     * Deactivate a thermal monitor
     * This is used by the cron job that changes monitor statuses based on monitor settings
     *
     * @param thermalMonitorEntity
     */
    suspend fun deactivateThermalMonitor(thermalMonitorEntity: ThermalMonitorEntity): ThermalMonitorEntity {
        thermalMonitorEntity.status = ThermalMonitorStatus.INACTIVE.toString()
        return persistSuspending(thermalMonitorEntity)
    }

    /**
     * List thermal monitors of type "SCHEDULED" and status "ACTIVE" that do not have any active schedules at the current time
     *
     * @param currentTime
     */
    suspend fun listScheduledActiveMonitorsWithoutActiveSchedules(currentTime: OffsetDateTime): List<ThermalMonitorEntity> {
        val query = """
                SELECT tm FROM ThermalMonitorEntity tm
                WHERE tm.monitorType = 'SCHEDULED'
                AND tm.status = 'ACTIVE'
                AND NOT EXISTS (
                    SELECT 1 FROM ThermalMonitorSchedulePeriodEntity sp
                    WHERE sp.thermalMonitor = tm
                    AND (
                        (sp.startWeekDay < :currentWeekDay OR (sp.startWeekDay = :currentWeekDay AND (sp.startHour < :currentHour OR (sp.startHour = :currentHour AND sp.startMinute <= :currentMinute))))
                        AND
                        (sp.endWeekDay > :currentWeekDay OR (sp.endWeekDay = :currentWeekDay AND (sp.endHour > :currentHour OR (sp.endHour = :currentHour AND sp.endMinute >= :currentMinute))))
                    )
                )
            """
        val parameters = Parameters.with("currentWeekDay", currentTime.dayOfWeek.value - 1)
            .and("currentHour", currentTime.hour)
            .and("currentMinute", currentTime.minute)

        return find(query, parameters).list<ThermalMonitorEntity>().awaitSuspending()
    }
}