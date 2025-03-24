package fi.metatavu.vp.monitoring.policies

import fi.metatavu.vp.api.model.ThermalMonitorIncidentStatus
import fi.metatavu.vp.api.model.ThermalMonitorPagingPolicy
import fi.metatavu.vp.api.spec.ThermalMonitorPagingPoliciesApi
import fi.metatavu.vp.monitoring.incidents.IncidentController
import fi.metatavu.vp.monitoring.monitors.ThermalMonitorController
import fi.metatavu.vp.monitoring.policies.contacts.PagingPolicyContactController
import fi.metatavu.vp.monitoring.rest.AbstractApi
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.*

@RequestScoped
@WithSession
class ThermalMonitorPagingPoliciesApiImpl: ThermalMonitorPagingPoliciesApi, AbstractApi() {
    @Inject
    lateinit var pagingPolicyController: PagingPolicyController

    @Inject
    lateinit var pagingPolicyTranslator: ThermalMonitorPagingPolicyTranslator

    @Inject
    lateinit var thermalMonitorController: ThermalMonitorController

    @Inject
    lateinit var pagingPolicyContactController: PagingPolicyContactController

    @Inject
    lateinit var incidentController: IncidentController

    @ConfigProperty(name = "vp.monitoring.cron.apiKey")
    lateinit var cronKey: String

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun createPagingPolicy(
        thermalMonitorId: UUID,
        thermalMonitorPagingPolicy: ThermalMonitorPagingPolicy
    ): Uni<Response> = withCoroutineScope {
        if (loggedUserId == null) {
            return@withCoroutineScope createUnauthorized(UNAUTHORIZED)
        }

        val thermalMonitor = thermalMonitorController.find(thermalMonitorId) ?:
            return@withCoroutineScope createNotFound(NOT_FOUND_MESSAGE)

        val contactId = thermalMonitorPagingPolicy.contactId
        val pagingPolicyContact = pagingPolicyContactController.find(contactId) ?:
            return@withCoroutineScope createBadRequest("Paging policy contact $contactId does not exist")

        createOk(pagingPolicyTranslator.translate(pagingPolicyController.create(
            thermalMonitor = thermalMonitor,
            pagingPolicyContact = pagingPolicyContact,
            policyType = thermalMonitorPagingPolicy.type,
            priority = thermalMonitorPagingPolicy.priority,
            escalationSeconds = thermalMonitorPagingPolicy.escalationDelaySeconds,
            creatorId = loggedUserId!!
        )))
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun deletePagingPolicy(thermalMonitorId: UUID, pagingPolicyId: UUID): Uni<Response> = withCoroutineScope {
        val policy = pagingPolicyController.find(pagingPolicyId) ?: return@withCoroutineScope createNotFound(NOT_FOUND_MESSAGE)

        if (policy.thermalMonitor.id != thermalMonitorId) {
            return@withCoroutineScope createNotFound(NOT_FOUND_MESSAGE)
        }

        pagingPolicyController.delete(policy)

        createNoContent()
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun findPagingPolicy(thermalMonitorId: UUID, pagingPolicyId: UUID): Uni<Response> = withCoroutineScope {
        val policy = pagingPolicyController.find(pagingPolicyId) ?: return@withCoroutineScope createNotFound(NOT_FOUND_MESSAGE)

        if (policy.thermalMonitor.id != thermalMonitorId) {
            return@withCoroutineScope createNotFound(NOT_FOUND_MESSAGE)
        }

        createOk(pagingPolicyTranslator.translate(policy))
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun listPagingPolicies(thermalMonitorId: UUID, first: Int, max: Int): Uni<Response> = withCoroutineScope {
        val thermalMonitor = thermalMonitorController.find(thermalMonitorId) ?:
            return@withCoroutineScope createNotFound(NOT_FOUND_MESSAGE)


        val policies = pagingPolicyController.list(
            thermalMonitor = thermalMonitor,
            first = first,
            max = max
        )

        createOk(policies.map { pagingPolicyTranslator.translate(it) })
    }

    @WithTransaction
    override fun triggerPolicies(): Uni<Response> = withCoroutineScope {
        if (requestCronKey != cronKey) {
            return@withCoroutineScope createUnauthorized(UNAUTHORIZED)
        }

        val triggeredIncident = incidentController.list(
            incidentStatus = ThermalMonitorIncidentStatus.TRIGGERED
        )

        triggeredIncident.forEach { pagingPolicyController.triggerNextPolicy(it) }

        createOk()
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun updatePagingPolicy(
        thermalMonitorId: UUID,
        pagingPolicyId: UUID,
        thermalMonitorPagingPolicy: ThermalMonitorPagingPolicy
    ): Uni<Response> = withCoroutineScope {
        if (loggedUserId == null) {
            return@withCoroutineScope createUnauthorized(UNAUTHORIZED)
        }

        val policy = pagingPolicyController.find(pagingPolicyId) ?: return@withCoroutineScope createNotFound(NOT_FOUND_MESSAGE)

        if (policy.thermalMonitor.id != thermalMonitorId) {
            return@withCoroutineScope createNotFound(NOT_FOUND_MESSAGE)
        }

        val contactId = thermalMonitorPagingPolicy.contactId
        val pagingPolicyContact = pagingPolicyContactController.find(contactId) ?:
            return@withCoroutineScope createBadRequest("Paging policy contact $contactId does not exist")

        createOk(pagingPolicyTranslator.translate(pagingPolicyController.update(
            entityToUpdate = policy,
            policyType = thermalMonitorPagingPolicy.type,
            priority = thermalMonitorPagingPolicy.priority,
            escalationDelaySeconds = thermalMonitorPagingPolicy.escalationDelaySeconds,
            pagingPolicyContact = pagingPolicyContact,
            modifierId = loggedUserId!!
        )))
    }
}