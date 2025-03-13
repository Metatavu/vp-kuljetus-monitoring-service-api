package fi.metatavu.vp.monitoring.event

import fi.metatavu.vp.messaging.WithCoroutineScope
import fi.metatavu.vp.messaging.events.TemperatureGlobalEvent
import fi.metatavu.vp.monitoring.incidents.IncidentController
import fi.metatavu.vp.monitoring.monitors.ThermalMonitorEntity
import fi.metatavu.vp.monitoring.monitors.thermometers.MonitorThermometerController
import fi.metatavu.vp.monitoring.monitors.thermometers.MonitorThermometerEntity
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.quarkus.vertx.ConsumeEvent
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger
import java.time.OffsetDateTime

/**
 * Event bus consumer for temperature events
 */
@ApplicationScoped
class TemperatureGlobalEventConsumer: WithCoroutineScope() {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var monitorThermometerController: MonitorThermometerController

    @Inject
    lateinit var incidentController: IncidentController

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

        val activeMonitorThermometers = monitorThermometerController.listThermometers(
            thermometerId = temperatureEvent.thermometerId,
            thermalMonitorEntity = null,
            onlyActive = true
        )

        activeMonitorThermometers.forEach{
            monitorThermometerController.updateThermometerLastMeasuredAt(it, temperatureEvent.timestamp)

            handleThresholds(temperatureEvent.temperature, it)
        }

        return@withCoroutineScope true
    }


    /**
     * Checks if a temperature event triggers an incident based on monitor threshold settings
     *
     * @param temperature
     * @param monitorThermometer
     */
    private suspend fun handleThresholds(temperature: Float, monitorThermometer: MonitorThermometerEntity) {
        val thermalMonitor = monitorThermometer.thermalMonitor
        val thresholdLow = thermalMonitor.thresholdLow
        val thresholdHigh = thermalMonitor.thresholdHigh

        val triggerThresholdIncident = (thresholdHigh != null && thresholdHigh > temperature) || (thresholdLow != null && thresholdLow < temperature)


        if (!triggerThresholdIncident) {
            return
        }

        incidentController.create(
                thermalMonitor = thermalMonitor,
                monitorThermometer = monitorThermometer,
                temperature = temperature
        )

    }

}