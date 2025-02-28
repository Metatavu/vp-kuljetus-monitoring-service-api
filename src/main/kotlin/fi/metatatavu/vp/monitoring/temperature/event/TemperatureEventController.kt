package fi.metatatavu.vp.monitoring.temperature.event

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class TemperatureEventController {
    @Inject
    lateinit var temperatureEventRepository: TemperatureEventRepository

    /**
     * Save the incoming temperature event. Remove the old events for the same sensor.
     *
     * @param sensorId
     * @param temperature
     * @param timeStamp
     */
    suspend fun saveEvent(sensorId: String, temperature: Float, timeStamp: Long): TemperatureEventEntity {
        val existingEvents = listBySensorId(sensorId)
        existingEvents.forEach {
            temperatureEventRepository.deleteSuspending(it)
        }

        return temperatureEventRepository.create(sensorId, temperature, timeStamp)
    }

    /**
     * List sensor events by sensor id
     *
     * @param sensorId
     */
    suspend fun listBySensorId(sensorId: String): List<TemperatureEventEntity> {
        return temperatureEventRepository.listBySensorId(sensorId)
    }
}