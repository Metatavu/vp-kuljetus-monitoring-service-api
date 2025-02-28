package fi.metatatavu.vp.monitoring.temperature.event

import fi.metatatavu.vp.monitoring.persistence.AbstractRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class TemperatureEventRepository: AbstractRepository<TemperatureEventEntity, UUID>() {
    /**
     * Save a temperature event to repository
     *
     * @param sensorId
     * @param temperature
     * @param timeStamp
     */
    suspend fun create(sensorId: String, temperature: Float, timeStamp: Long): TemperatureEventEntity {
        val temperatureEventEntity = TemperatureEventEntity()
        temperatureEventEntity.id = UUID.randomUUID()
        temperatureEventEntity.temperature = temperature
        temperatureEventEntity.timestamp = timeStamp
        temperatureEventEntity.sensorId = sensorId
        return persistSuspending(temperatureEventEntity)
    }
}