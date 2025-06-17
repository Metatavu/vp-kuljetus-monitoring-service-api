package fi.metatavu.vp.monitoring.event

import fi.metatavu.vp.api.model.ThermalMonitorIncidentStatus
import fi.metatavu.vp.messaging.WithCoroutineScope
import fi.metatavu.vp.monitoring.incidents.IncidentController
import fi.metatavu.vp.monitoring.policies.PagingPolicyController
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.quarkus.vertx.ConsumeEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject


@ApplicationScoped
class TriggerPoliciesEventConsumer: WithCoroutineScope() {

    @Inject
    lateinit var pagingPolicyController: PagingPolicyController

    @Inject
    lateinit var incidentController: IncidentController

    @ConsumeEvent("TRIGGER_POLICIES")
    @Suppress("unused")
    @WithTransaction
    fun onEvent(triggerPoliciesEvent: TriggerPoliciesEvent) = withCoroutineScope(60_000) {
        val triggeredIncidents = incidentController.list(
            incidentStatus = ThermalMonitorIncidentStatus.TRIGGERED
        ).first

        triggeredIncidents.forEach { pagingPolicyController.triggerNextPolicy(it) }

        return@withCoroutineScope true
    }
}