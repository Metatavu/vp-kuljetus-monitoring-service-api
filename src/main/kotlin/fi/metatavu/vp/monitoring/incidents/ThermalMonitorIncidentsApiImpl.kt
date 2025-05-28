package fi.metatavu.vp.monitoring.incidents

import fi.metatavu.vp.api.model.ThermalMonitorIncident
import fi.metatavu.vp.api.model.ThermalMonitorIncidentStatus
import fi.metatavu.vp.api.spec.ThermalMonitorIncidentsApi
import fi.metatavu.vp.monitoring.event.CreateLostSensorIncidentsEvent
import fi.metatavu.vp.monitoring.event.TriggerPoliciesEvent
import fi.metatavu.vp.monitoring.monitors.ThermalMonitorController
import fi.metatavu.vp.monitoring.monitors.ThermalMonitorEntity
import fi.metatavu.vp.monitoring.monitors.thermometers.MonitorThermometerController
import fi.metatavu.vp.monitoring.rest.AbstractApi
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import io.vertx.mutiny.core.eventbus.EventBus
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.time.OffsetDateTime
import java.util.*

@RequestScoped
@WithSession
class ThermalMonitorIncidentsApiImpl: ThermalMonitorIncidentsApi, AbstractApi() {
    @Inject
    lateinit var incidentController: IncidentController

    @Inject
    lateinit var thermalMonitorController: ThermalMonitorController

    @Inject
    lateinit var thermalMonitorIncidentTranslator: ThermalMonitorIncidentTranslator

    @ConfigProperty(name = "vp.monitoring.cron.apiKey")
    lateinit var cronKey: String

    @Inject
    lateinit var eventBus: EventBus

    @WithTransaction
    override fun createLostSensorIncidents(): Uni<Response> = withCoroutineScope {
        if (requestCronKey != cronKey) {
            return@withCoroutineScope createUnauthorized(UNAUTHORIZED)
        }

        eventBus.send("CREATE_LOST_SENSOR_INCIDENTS", CreateLostSensorIncidentsEvent())

        createOk()
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun listThermalMonitorIncidents(
        monitorId: UUID?,
        thermometerId: UUID?,
        incidentStatus: ThermalMonitorIncidentStatus?,
        after: OffsetDateTime?,
        before: OffsetDateTime?,
        first: Int?,
        max: Int?
    ): Uni<Response> = withCoroutineScope {

        val monitor = monitorId?.let { thermalMonitorController.find(monitorId) }

        createOk(incidentController.list(
            thermalMonitor = monitor,
            thermometerId = thermometerId,
            incidentStatus = incidentStatus,
            triggeredAfter = after,
            triggeredBefore = before,
            first = first,
            max = max
        ).map { thermalMonitorIncidentTranslator.translate(it) })
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun updateThermalMonitorIncident(
        thermalMonitorIncidentId: UUID,
        thermalMonitorIncident: ThermalMonitorIncident
    ): Uni<Response> = withCoroutineScope {
        if (loggedUserId == null) {
            return@withCoroutineScope createUnauthorized(UNAUTHORIZED)
        }

        val found = incidentController.find(thermalMonitorIncidentId) ?: return@withCoroutineScope createNotFound(NOT_FOUND_MESSAGE)

        if (found.status == ThermalMonitorIncidentStatus.RESOLVED.toString()) {
            return@withCoroutineScope createBadRequest("Updating resolved incidents is forbidden")
        }

        if (thermalMonitorIncident.status == ThermalMonitorIncidentStatus.TRIGGERED) {
            return@withCoroutineScope createBadRequest("Updating status to TRIGGERED is forbidden")
        }

        if (thermalMonitorIncident.status == null) {
            return@withCoroutineScope createBadRequest("Status is required")
        }

        createOk(thermalMonitorIncidentTranslator.translate(incidentController.updateIncidentStatus(
            incident = found,
            newStatus = thermalMonitorIncident.status,
            modifier = loggedUserId!!
        )))
    }
}