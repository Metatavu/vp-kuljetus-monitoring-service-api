package fi.metatavu.vp.monitoring.temperature.event

import fi.metatavu.vp.monitoring.persistence.AbstractRepository
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class TemperatureEventRepository: AbstractRepository<TemperatureEventEntity, UUID>() {
    /**
     * Save a temperature event to repository
     *
     * @param sensorId
     * @param timeStamp
     */
    suspend fun create(sensorId: String, timeStamp: Long): TemperatureEventEntity {
        val temperatureEventEntity = TemperatureEventEntity()
        temperatureEventEntity.id = UUID.randomUUID()
        temperatureEventEntity.timestamp = timeStamp
        temperatureEventEntity.sensorId = sensorId
        return persistSuspending(temperatureEventEntity)
    }

    /**
     * List temperature events by sensor id
     *
     *
     * @param sensorId
     */
    suspend fun listBySensorId(sensorId: String): List<TemperatureEventEntity> {
        return find("sensorId = :sensorId", Parameters().and("sensorId", sensorId)).list<TemperatureEventEntity>().awaitSuspending()
    }
}