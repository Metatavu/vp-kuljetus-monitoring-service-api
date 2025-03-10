package fi.metatavu.vp.monitoring.policies.contacts

import fi.metatavu.vp.api.model.PagingPolicyContact
import fi.metatavu.vp.api.spec.PagingPolicyContactsApi
import fi.metatavu.vp.monitoring.rest.AbstractApi
import io.smallrye.mutiny.Uni
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import java.util.*

class PagingPolicyContactApiImpl : PagingPolicyContactsApi, AbstractApi() {
    @Inject
    lateinit var pagingPolicyContactController: PagingPolicyContactController

    @Inject
    lateinit var pagingPolicyContactTranslator: PagingPolicyContactTranslator

    @RolesAllowed(MANAGER_ROLE)
    @Transactional
    override fun createPagingPolicyContact(pagingPolicyContact: PagingPolicyContact): Uni<Response> = withCoroutineScope {
        if (loggedUserId == null) {
            return@withCoroutineScope createUnauthorized(UNAUTHORIZED)
        }

        val contactEntity = pagingPolicyContactController.create(
            name = pagingPolicyContact.name,
            email = pagingPolicyContact.name,
            creatorId = loggedUserId!!
        )

        createOk(pagingPolicyContactTranslator.translate(contactEntity))
    }

    @RolesAllowed(MANAGER_ROLE)
    @Transactional
    override fun deletePagingPolicyContact(pagingPolicyContactId: UUID): Uni<Response> = withCoroutineScope {
        val contact = pagingPolicyContactController.find(pagingPolicyContactId) ?: return@withCoroutineScope createNotFound(NOT_FOUND_MESSAGE)

        pagingPolicyContactController.delete(contact)

        createNoContent()
    }

    @RolesAllowed(MANAGER_ROLE)
    @Transactional
    override fun findPagingPolicyContact(pagingPolicyContactId: UUID): Uni<Response> = withCoroutineScope {
        val contact = pagingPolicyContactController.find(pagingPolicyContactId) ?: return@withCoroutineScope createNotFound(NOT_FOUND_MESSAGE)

        createOk(pagingPolicyContactTranslator.translate(contact))
    }

    override fun listPagingPolicyContacts(first: Int?, max: Int?): Uni<Response> {
        TODO("Not yet implemented")
    }

    override fun updatePagingPolicyContact(
        pagingPolicyContactId: UUID,
        pagingPolicyContact: PagingPolicyContact
    ): Uni<Response> {
        TODO("Not yet implemented")
    }
}