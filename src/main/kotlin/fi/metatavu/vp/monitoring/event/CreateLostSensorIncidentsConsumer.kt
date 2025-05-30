package fi.metatavu.vp.monitoring.event

import io.quarkus.vertx.ConsumeEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import fi.metatavu.vp.monitoring.incidents.IncidentController
import fi.metatavu.vp.messaging.WithCoroutineScope
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni


@ApplicationScoped
class CreateLostSensorIncidentsConsumer: WithCoroutineScope() {
    @Inject
    lateinit var incidentController: IncidentController

    @ConsumeEvent("CREATE_LOST_SENSOR_INCIDENTS")
    @WithTransaction
    @Suppress("unused")
    fun onEvent(event: CreateLostSensorIncidentsEvent): Uni<Boolean> = withCoroutineScope(60_000) {
        incidentController.createLostSensorIncidents()

        return@withCoroutineScope true
    }
}