package fi.metatavu.vp.monitoring.policies.contacts

import fi.metatavu.vp.api.model.PagingPolicyContact
import fi.metatavu.vp.api.model.ThermalMonitorStatus
import fi.metatavu.vp.monitoring.monitors.ThermalMonitorEntity
import fi.metatavu.vp.monitoring.persistence.AbstractRepository
import io.quarkus.panache.common.Parameters
import jakarta.enterprise.context.ApplicationScoped
import java.time.OffsetDateTime
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
        contact.contactName = name
        contact.email = email
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
        entityToUpdate.email = entityFromRest.email
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