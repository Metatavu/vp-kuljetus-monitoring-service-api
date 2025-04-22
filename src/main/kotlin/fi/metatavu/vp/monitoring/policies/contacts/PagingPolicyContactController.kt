package fi.metatavu.vp.monitoring.policies.contacts

import fi.metatavu.vp.api.model.PagingPolicyContact
import fi.metatavu.vp.api.model.PagingPolicyType
import fi.metatavu.vp.monitoring.policies.PagingPolicyController
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

@ApplicationScoped
class PagingPolicyContactController {

    @Inject
    lateinit var pagingPolicyContactRepository: PagingPolicyContactRepository

    @Inject
    lateinit var pagingPolicyController: PagingPolicyController

    /**
     * Save a paging policy contact to the database
     *
     * @param name
     * @param contactType
     * @param contact
     * @param creatorId
     */
    suspend fun create(name: String, contactType: PagingPolicyType, contact: String, creatorId: UUID): PagingPolicyContactEntity {
        return pagingPolicyContactRepository.create(
            name = name,
            contactValue = contact,
            creatorId = creatorId,
            contactType = contactType.toString()
        )
    }

    /**
     * Delete a paging policy contact from the database
     *
     * @param contact
     */
    suspend fun delete(contact: PagingPolicyContactEntity) {
        pagingPolicyController.deletePoliciesByContact(contact)

        pagingPolicyContactRepository.deleteSuspending(contact)
    }

    /**
     * Retrieve a paging policy contact by id if exists
     *
     * @param id
     */
    suspend fun find(id: UUID): PagingPolicyContactEntity? {
        return pagingPolicyContactRepository.findByIdSuspending(id)
    }

    /**
     * List paging policies
     *
     * @param first first item index
     * @param max max amount items
     */
    suspend fun list(first: Int?, max: Int?): List<PagingPolicyContactEntity> {
        return pagingPolicyContactRepository.list(first, max).first
    }

    /**
     * Update paging policy
     *
     * @param entityToUpdate
     * @param entityFromRest
     * @param modifierId
     */
    suspend fun update(entityToUpdate: PagingPolicyContactEntity, entityFromRest: PagingPolicyContact, modifierId: UUID): PagingPolicyContactEntity {
        return pagingPolicyContactRepository.update(
            entityToUpdate = entityToUpdate,
            entityFromRest = entityFromRest,
            modifierId = modifierId
        )
    }
}