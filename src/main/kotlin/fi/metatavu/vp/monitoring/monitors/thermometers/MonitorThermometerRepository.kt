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
        thermometer.creatorId = creatorId
        thermometer.lastModifierId = creatorId
        return persistSuspending(thermometer)
    }

    /**
     * Lists thermometers
     *
     *  @param thermalMonitorEntity
     *  @param thermometerId
     */
    suspend fun listThermometers(thermalMonitorEntity: ThermalMonitorEntity?, thermometerId: UUID?): List<MonitorThermometerEntity> {
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

        return find(queryBuilder.toString(), parameters).list<MonitorThermometerEntity>().awaitSuspending()
    }

    /**
     * Update the time information about when this thermometer sent the latest measurement
     * This is done when a new temperature event is received
     *
     * @param monitorThermometerEntity
     */
    suspend fun updateThermometerLastMeasuredAt(monitorThermometerEntity: MonitorThermometerEntity, lastMeasuredAt: Long) {
        monitorThermometerEntity.lastMeasuredAt = lastMeasuredAt
        persistSuspending(monitorThermometerEntity)
    }
}