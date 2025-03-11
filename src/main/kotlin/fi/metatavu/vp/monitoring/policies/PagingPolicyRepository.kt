package fi.metatavu.vp.monitoring.policies

import fi.metatavu.vp.api.model.PagingPolicyContact
import fi.metatavu.vp.monitoring.persistence.AbstractRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class PagingPolicyRepository: AbstractRepository<ThermalMonitorPagingPolicyEntity, UUID>() {

    /**
     * Save a thermal monitor paging policy to the database
     *
     * @param policyType
     * @param priority
     * @param escalationSeconds
     * @param pagingPolicyContact
     * @param creatorId
     */
    suspend fun create(
        policyType: String,
        priority: Int,
        escalationSeconds: Int,
        pagingPolicyContact: PagingPolicyContact,
        creatorId: UUID
    ): ThermalMonitorPagingPolicyEntity {
        val policy = ThermalMonitorPagingPolicyEntity()

        policy.id = UUID.randomUUID()
        policy.policyType = policyType
        policy.priority = priority
        policy.escalationDelaySeconds = escalationSeconds
        policy.pagingPolicyContact = pagingPolicyContact
        policy.creatorId = creatorId
        policy.lastModifierId = creatorId

        return persistSuspending(policy)
    }
}