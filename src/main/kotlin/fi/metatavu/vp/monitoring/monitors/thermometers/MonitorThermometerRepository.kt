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
    suspend fun create(thermometerId: UUID, thermalMonitorEntity: ThermalMonitorEntity, creatorId: UUID): MonitorThermometerEntity {
        val thermometer = MonitorThermometerEntity()
        thermometer.id = UUID.randomUUID()
        thermometer.thermometerId = thermometerId
        thermometer.thermalMonitor = thermalMonitorEntity
        thermometer.creatorId = creatorId
        thermometer.lastModifierId = creatorId
        return persistSuspending(thermometer)
    }

    suspend fun list(thermalMonitorEntity: ThermalMonitorEntity): List<MonitorThermometerEntity> {
        val queryBuilder = StringBuilder()
        val parameters = Parameters()

        queryBuilder.append("thermalMonitor = :monitor")
        parameters.and("monitor", thermalMonitorEntity)

        return find(queryBuilder.toString(), parameters).list<MonitorThermometerEntity>().awaitSuspending()
    }
}