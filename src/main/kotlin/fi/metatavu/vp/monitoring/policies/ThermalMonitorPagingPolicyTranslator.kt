package fi.metatavu.vp.monitoring.policies

import fi.metatavu.vp.api.model.PagingPolicyType
import fi.metatavu.vp.api.model.ThermalMonitorPagingPolicy
import fi.metatavu.vp.usermanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ThermalMonitorPagingPolicyTranslator: AbstractTranslator<ThermalMonitorPagingPolicyEntity, ThermalMonitorPagingPolicy>() {

    /**
     * Translate a thermal monitor paging policy database entity into a REST entity
     *
     * @param entity
     */
    override suspend fun translate(entity: ThermalMonitorPagingPolicyEntity): ThermalMonitorPagingPolicy {
        return ThermalMonitorPagingPolicy(
            type = PagingPolicyType.valueOf(entity.policyType),
            contactId = entity.pagingPolicyContact.id,
            priority = entity.priority!!,
            escalationDelaySeconds = entity.escalationDelaySeconds!!
        )
    }
}