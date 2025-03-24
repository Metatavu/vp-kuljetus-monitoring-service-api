package fi.metatavu.vp.monitoring.incidents.pagedpolicies

import fi.metatavu.vp.monitoring.incidents.ThermalMonitorIncidentEntity
import fi.metatavu.vp.monitoring.monitors.ThermalMonitorEntity
import fi.metatavu.vp.monitoring.persistence.AbstractRepository
import fi.metatavu.vp.monitoring.policies.ThermalMonitorPagingPolicyEntity
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.time.OffsetDateTime
import java.util.UUID

@ApplicationScoped
class PagedPolicyRepository: AbstractRepository<PagedPolicyEntity, UUID>() {
    /**
     * Saves a paged policy to the database
     *
     * @param incident
     * @param policy
     */
    suspend fun create(
        incident: ThermalMonitorIncidentEntity,
        policy: ThermalMonitorPagingPolicyEntity
    ): PagedPolicyEntity {
        val pagedPolicy = PagedPolicyEntity()
        pagedPolicy.id = UUID.randomUUID()
        pagedPolicy.incident = incident
        pagedPolicy.policy = policy
        pagedPolicy.createdAt = OffsetDateTime.now()
        return persistSuspending(pagedPolicy)
    }

    /**
     * List paged policies that belong to a given incident
     *
     * @param incident
     */
    suspend fun listByIncident(incident: ThermalMonitorIncidentEntity): List<PagedPolicyEntity> {
        val queryBuilder = StringBuilder()
        val parameters = Parameters()

        addCondition(queryBuilder, "incident = :incident")
        parameters.and("incident", incident)

        return list(queryBuilder.toString(), Sort.descending("createdAt"), parameters).awaitSuspending()
    }

    /**
     * List paged policies that belong to a given policy
     *
     * @param policy
     */
    suspend fun listByPolicy(policy: ThermalMonitorPagingPolicyEntity): List<PagedPolicyEntity> {
        val queryBuilder = StringBuilder()
        val parameters = Parameters()

        addCondition(queryBuilder, "policy = :policy")
        parameters.and("policy", policy)

        return list(queryBuilder.toString(), Sort.descending("createdAt"), parameters).awaitSuspending()
    }
}