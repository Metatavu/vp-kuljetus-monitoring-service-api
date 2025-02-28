package fi.metatatavu.vp.monitoring.event

import fi.metatatavu.vp.monitoring.temperature.event.TemperatureEventController
import fi.metatavu.vp.messaging.WithCoroutineScope
import fi.metatavu.vp.messaging.events.TemperatureGlobalEvent
import io.quarkus.vertx.ConsumeEvent
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger

/**
 * Event bus consumer for temperature events
 */
@ApplicationScoped
class TemperatureGlobalEventConsumer: WithCoroutineScope() {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var temperatureEventController: TemperatureEventController

    /**
     * Event bus consumer for temperature events
     *
     * @param temperatureEvent temperature event
     */
    @ConsumeEvent(TEMPERATURE_EVENT)
    @Suppress("unused")
    fun onTemperatureEvent(temperatureEvent: TemperatureGlobalEvent): Uni<Void> = withCoroutineScope {
        logger.info("Temperature event: $temperatureEvent")

        temperatureEventController.create(sensorId = temperatureEvent.sensorId, temperature = temperatureEvent.temperature, timeStamp = temperatureEvent.timestamp)
    }.replaceWithVoid()

    companion object {
        const val TEMPERATURE_EVENT = "temperature-event"
    }
}