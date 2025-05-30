package fi.metatavu.vp.monitoring.event

import fi.metatavu.vp.messaging.WithCoroutineScope
import fi.metatavu.vp.monitoring.monitors.ThermalMonitorController
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.quarkus.vertx.ConsumeEvent
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class ResolveMonitorStatusesEventConsumer: WithCoroutineScope() {
    @Inject
    lateinit var thermalMonitorController: ThermalMonitorController

    @ConsumeEvent("RESOLVE_MONITOR_STATUSES")
    @Suppress("unused")
    @WithTransaction
    fun onEvent(resolveMonitorStatusesEvent: ResolveMonitorStatusesEvent): Uni<Boolean> = withCoroutineScope(60_000) {

        thermalMonitorController.resolveOneOffMonitorStatuses()

        thermalMonitorController.resolveScheduledMonitorStatuses()

        return@withCoroutineScope true
    }
}