package fi.metatavu.vp.monitoring.policies.contacts

import fi.metatavu.vp.api.model.PagingPolicyContact
import fi.metatavu.vp.usermanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class PagingPolicyContactTranslator: AbstractTranslator<PagingPolicyContactEntity, PagingPolicyContact>() {

    /**
     * Translate paging policy contact from a database entity to a REST entity
     *
     * @param entity
     */
    override suspend fun translate(entity: PagingPolicyContactEntity): PagingPolicyContact {
        return PagingPolicyContact(
            id = entity.id,
            name = entity.contactName,
            email = entity.email
        )
    }

}