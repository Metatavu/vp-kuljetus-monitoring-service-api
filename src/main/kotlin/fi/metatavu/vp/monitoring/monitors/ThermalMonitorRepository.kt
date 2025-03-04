package fi.metatavu.vp.monitoring.monitors

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
     */
    suspend fun create(
        name: String,
        status: String,
        creatorId: UUID,
        thresholdLow: Float?,
        thresholdHigh: Float?,
        activeFrom: OffsetDateTime?,
        activeTo: OffsetDateTime?
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
        return persistSuspending(monitor)
    }

    suspend fun list(
        status: ThermalMonitorStatus?,
        activeAfter: OffsetDateTime?,
        activeBefore: OffsetDateTime?,
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

        return applyFirstMaxToQuery(find(queryBuilder.toString(), parameters), firstIndex = first, maxResults = max)
    }
}