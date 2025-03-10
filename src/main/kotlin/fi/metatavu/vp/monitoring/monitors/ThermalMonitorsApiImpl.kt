package fi.metatavu.vp.monitoring.monitors

import fi.metatavu.vp.api.model.ThermalMonitor
import fi.metatavu.vp.api.model.ThermalMonitorStatus
import fi.metatavu.vp.api.spec.ThermalMonitorsApi
import fi.metatavu.vp.monitoring.rest.AbstractApi
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import java.time.OffsetDateTime
import java.util.*

@RequestScoped
@WithSession
class ThermalMonitorsApiImpl: ThermalMonitorsApi, AbstractApi() {
    @Inject
    lateinit var thermalMonitorController: ThermalMonitorController

    @Inject
    lateinit var thermalMonitorTranslator: ThermalMonitorTranslator

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun createThermalMonitor(thermalMonitor: ThermalMonitor): Uni<Response> = withCoroutineScope {
        loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)

        createOk(thermalMonitorTranslator.translate(thermalMonitorController.create(
            thermalMonitor = thermalMonitor,
            creatorId = loggedUserId!!
        )))
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun deleteThermalMonitor(thermalMonitorId: UUID): Uni<Response> = withCoroutineScope {
        val found = thermalMonitorController.find(thermalMonitorId) ?: return@withCoroutineScope createNotFound()
        thermalMonitorController.delete(found)

        createNoContent()
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun findThermalMonitor(thermalMonitorId: UUID): Uni<Response> = withCoroutineScope {
        val found = thermalMonitorController.find(thermalMonitorId) ?: return@withCoroutineScope createNotFound()
        createOk(thermalMonitorTranslator.translate(found))
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun listThermalMonitors(
        status: ThermalMonitorStatus?,
        activeAfter: OffsetDateTime?,
        activeBefore: OffsetDateTime?,
        first: Int?,
        max: Int?
    ): Uni<Response> = withCoroutineScope {
        val list = thermalMonitorController.list(status, activeBefore, activeAfter, first, max)

        createOk(list.map{ thermalMonitorEntity -> thermalMonitorTranslator.translate(thermalMonitorEntity) })
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun updateThermalMonitor(thermalMonitorId: UUID, thermalMonitor: ThermalMonitor): Uni<Response> = withCoroutineScope {
        loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)

        val found = thermalMonitorController.find(thermalMonitorId) ?: return@withCoroutineScope createNotFound()

        createOk(thermalMonitorTranslator.translate(thermalMonitorController.update(thermalMonitor, found, loggedUserId!!)))
    }
}