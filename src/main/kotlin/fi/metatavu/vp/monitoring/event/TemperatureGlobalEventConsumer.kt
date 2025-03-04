package fi.metatavu.vp.monitoring.event

import fi.metatavu.vp.monitoring.temperature.event.TemperatureEventController
import fi.metatavu.vp.messaging.GlobalEventController
import fi.metatavu.vp.messaging.WithCoroutineScope
import fi.metatavu.vp.messaging.events.TemperatureGlobalEvent
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
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
    lateinit var temperatureEventController: fi.metatavu.vp.monitoring.temperature.event.TemperatureEventController

    @Inject
    lateinit var globalEventController: GlobalEventController

    /**
     * Event bus consumer for temperature events
     *
     * @param temperatureEvent temperature event
     */
    @ConsumeEvent("TEMPERATURE")
    @Suppress("unused")
    @WithTransaction
    fun onTemperatureEvent(temperatureEvent: TemperatureGlobalEvent): Uni<Boolean> = withCoroutineScope {
        logger.info("Temperature event: $temperatureEvent")

        temperatureEventController.saveEvent(sensorId = temperatureEvent.sensorId, temperature = temperatureEvent.temperature, timeStamp = temperatureEvent.timestamp)

        return@withCoroutineScope true
    }

}