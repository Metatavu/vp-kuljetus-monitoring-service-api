package fi.metatavu.vp.monitoring.policies

import fi.metatavu.vp.api.model.PagingPolicyContact
import fi.metatavu.vp.api.model.PagingPolicyType
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

@ApplicationScoped
class PagingPolicyController {

    @Inject
    lateinit var pagingPolicyRepository: PagingPolicyRepository

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
        policyType: PagingPolicyType,
        priority: Int,
        escalationSeconds: Int,
        pagingPolicyContact: PagingPolicyContact,
        creatorId: UUID
    ): ThermalMonitorPagingPolicyEntity {
        return pagingPolicyRepository.create(
            policyType = policyType.toString(),
            priority = priority,
            escalationSeconds = escalationSeconds,
            pagingPolicyContact = pagingPolicyContact,
            creatorId = creatorId
        )
    }

}