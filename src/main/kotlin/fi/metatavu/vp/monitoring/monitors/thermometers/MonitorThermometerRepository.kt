package fi.metatavu.vp.monitoring.monitors.thermometers

import fi.metatavu.vp.monitoring.monitors.ThermalMonitorEntity
import fi.metatavu.vp.monitoring.persistence.AbstractRepository
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

/**
 * Database operations for monitor thermometers
 */
@ApplicationScoped
class MonitorThermometerRepository: AbstractRepository<MonitorThermometerEntity, UUID>() {
    /**
     * Save thermal monitor to the database
     *
     * @param thermometerId
     * @param thermalMonitorEntity
     * @param creatorId
     */
    suspend fun create(thermometerId: UUID, thermalMonitorEntity: ThermalMonitorEntity, creatorId: UUID): MonitorThermometerEntity {
        val thermometer = MonitorThermometerEntity()
        thermometer.id = UUID.randomUUID()
        thermometer.thermometerId = thermometerId
        thermometer.thermalMonitor = thermalMonitorEntity
        thermometer.archived = false
        thermometer.creatorId = creatorId
        thermometer.lastModifierId = creatorId
        return persistSuspending(thermometer)
    }

    /**
     * Lists thermometers
     *
     *  @param thermalMonitorEntity
     *  @param thermometerId
     *  @param onlyActive
     *  @param lastMeasuredBefore
     */
    suspend fun listThermometers(
        thermalMonitorEntity: ThermalMonitorEntity?,
        thermometerId: UUID?,
        onlyActive: Boolean,
        includeArchived: Boolean,
        lastMeasuredBefore: Long?
        ): List<MonitorThermometerEntity> {
        val queryBuilder = StringBuilder()
        val parameters = Parameters()

        if (thermalMonitorEntity != null) {
            addCondition(queryBuilder, "thermalMonitor = :monitor")
            parameters.and("monitor", thermalMonitorEntity)
        }

        if (thermometerId != null) {
            addCondition(queryBuilder, "thermometerId = :thermometerId")
            parameters.and("thermometerId", thermometerId)
        }

        if (onlyActive) {
            addCondition(queryBuilder, "thermalMonitor.status = 'ACTIVE'")
        }

        if (!includeArchived) {
            addCondition(queryBuilder, "archived = false")
        }

        if (lastMeasuredBefore != null) {
            addCondition(queryBuilder, "lastMeasuredAt < :lastMeasuredBefore")
            parameters.and("lastMeasuredBefore", lastMeasuredBefore)
        }

        return find(queryBuilder.toString(), parameters).list<MonitorThermometerEntity>().awaitSuspending()
    }

    /**
     * Update the time information about when this thermometer sent the latest measurement
     * This is done when a new temperature event is received
     *
     * @param monitorThermometerEntity
     */
    suspend fun updateThermometerLastMeasuredAt(monitorThermometerEntity: MonitorThermometerEntity, lastMeasuredAt: Long): MonitorThermometerEntity{
        monitorThermometerEntity.lastMeasuredAt = lastMeasuredAt
        return persistSuspending(monitorThermometerEntity)
    }

    /**
     * Archives a thermometer
     * This is done instead of deleting to keep the incident history
     *
     * @param monitorThermometerEntity
     * @param lastModifierId
     */
    suspend fun archiveThermometer(
        monitorThermometerEntity: MonitorThermometerEntity,
        lastModifierId: UUID
    ) {
        monitorThermometerEntity.archived = true
        monitorThermometerEntity.lastModifierId = lastModifierId
        persistSuspending(monitorThermometerEntity)
    }
}