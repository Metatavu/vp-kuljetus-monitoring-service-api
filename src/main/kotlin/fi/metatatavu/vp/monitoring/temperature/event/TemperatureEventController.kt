package fi.metatatavu.vp.monitoring.temperature.event

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

@ApplicationScoped
class TemperatureEventController {
    @Inject
    lateinit var temperatureEventRepository: TemperatureEventRepository

    /**
     * Save the incoming temperature event
     *
     * @param sensorId
     * @param temperature
     * @param timeStamp
     */
    suspend fun create(sensorId: String, temperature: Float, timeStamp: Long): TemperatureEventEntity {
        return temperatureEventRepository.create(sensorId, temperature, timeStamp)
    }
}