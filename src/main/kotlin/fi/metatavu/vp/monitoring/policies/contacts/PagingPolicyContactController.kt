package fi.metatavu.vp.monitoring.policies.contacts

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

@ApplicationScoped
class PagingPolicyContactController {

    @Inject
    lateinit var pagingPolicyContactRepository: PagingPolicyContactRepository

    /**
     * Save a paging policy contact to the database
     *
     * @param name
     * @param email
     * @param creatorId
     */
    suspend fun create(name: String?, email: String?, creatorId: UUID): PagingPolicyContactEntity {
        return pagingPolicyContactRepository.create(
            name = name,
            email = email,
            creatorId = creatorId
        )
    }

    /**
     * Delete a paging policy contact from the database
     *
     * @param contact
     */
    suspend fun delete(contact: PagingPolicyContactEntity) {
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
}