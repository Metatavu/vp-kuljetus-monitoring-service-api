package fi.metatavu.vp.monitoring.policies.contacts

import fi.metatavu.vp.api.model.PagingPolicyContact
import fi.metatavu.vp.monitoring.persistence.AbstractRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

@ApplicationScoped
class PagingPolicyContactRepository: AbstractRepository<PagingPolicyContactEntity, UUID>() {

    /**
     * Save a paging policy contact to the database
     *
     * @param name
     * @param contactType
     * @param contactValue
     * @param creatorId
     */
    suspend fun create(name: String, contactType: String, contactValue: String, creatorId: UUID): PagingPolicyContactEntity {
        val contact = PagingPolicyContactEntity()
        contact.id = UUID.randomUUID()
        contact.contactType = contactType
        contact.contactName = name
        contact.contact = contactValue
        contact.creatorId = creatorId
        contact.lastModifierId = creatorId

        return persistSuspending(contact)
    }

    /**
     * Update paging policy contact
     *
     * @param entityToUpdate
     * @param entityFromRest
     * @param modifierId
     */
    suspend fun update(
        entityToUpdate: PagingPolicyContactEntity,
        entityFromRest: PagingPolicyContact,
        modifierId: UUID
    ): PagingPolicyContactEntity {
        entityToUpdate.contact = entityFromRest.contact
        entityToUpdate.contactType = entityFromRest.type.toString()
        entityToUpdate.contactName = entityFromRest.name
        entityToUpdate.lastModifierId = modifierId

        return persistSuspending(entityToUpdate)
    }

    /**
     * List thermal monitors
     *
     * @param first
     * @param max
     */
    suspend fun list(
        first: Int?,
        max: Int?
    ): Pair<List<PagingPolicyContactEntity>, Long> {
        return applyFirstMaxToQuery(findAll(), firstIndex = first, maxResults = max)
    }
}