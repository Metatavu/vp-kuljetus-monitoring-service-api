package fi.metatavu.vp.monitoring.monitors

import fi.metatavu.vp.api.model.ThermalMonitor
import fi.metatavu.vp.api.model.ThermalMonitorStatus
import fi.metatavu.vp.monitoring.persistence.AbstractRepository
import io.quarkus.panache.common.Parameters
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
     * @param first
     * @param max
     */
    suspend fun list(
        status: ThermalMonitorStatus?,
        activeAfter: OffsetDateTime?,
        activeBefore: OffsetDateTime?,
        toBeActivatedBefore: OffsetDateTime?,
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
        updated.creatorId = thermalMonitor.creatorId!!
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
}