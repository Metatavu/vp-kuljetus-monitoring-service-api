package fi.metatavu.vp.monitoring.policies.contacts

import fi.metatavu.vp.monitoring.persistence.AbstractRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

@ApplicationScoped
class PagingPolicyContactRepository: AbstractRepository<PagingPolicyContactEntity, UUID>() {

    /**
     * Save a paging policy contact to the database
     *
     * @param name
     * @param email
     * @param creatorId
     */
    suspend fun create(name: String?, email: String?, creatorId: UUID): PagingPolicyContactEntity {
        val contact = PagingPolicyContactEntity()
        contact.id = UUID.randomUUID()
        contact.name = name
        contact.email = email
        contact.creatorId = creatorId
        contact.lastModifierId = creatorId

        return persistSuspending(contact)
    }
}