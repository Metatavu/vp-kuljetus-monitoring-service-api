package fi.metatavu.vp.monitoring.policies.contacts

import fi.metatavu.vp.api.model.PagingPolicyContact
import fi.metatavu.vp.api.spec.PagingPolicyContactsApi
import fi.metatavu.vp.monitoring.rest.AbstractApi
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import java.util.*

@RequestScoped
@WithSession
class PagingPolicyContactApiImpl : PagingPolicyContactsApi, AbstractApi() {
    @Inject
    lateinit var pagingPolicyContactController: PagingPolicyContactController

    @Inject
    lateinit var pagingPolicyContactTranslator: PagingPolicyContactTranslator

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun createPagingPolicyContact(pagingPolicyContact: PagingPolicyContact): Uni<Response> = withCoroutineScope {
        if (loggedUserId == null) {
            return@withCoroutineScope createUnauthorized(UNAUTHORIZED)
        }

        val contactEntity = pagingPolicyContactController.create(
            name = pagingPolicyContact.name,
            email = pagingPolicyContact.email,
            creatorId = loggedUserId!!
        )

        createOk(pagingPolicyContactTranslator.translate(contactEntity))
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun deletePagingPolicyContact(pagingPolicyContactId: UUID): Uni<Response> = withCoroutineScope {
        val contact = pagingPolicyContactController.find(pagingPolicyContactId) ?: return@withCoroutineScope createNotFound(NOT_FOUND_MESSAGE)

        pagingPolicyContactController.delete(contact)

        createNoContent()
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun findPagingPolicyContact(pagingPolicyContactId: UUID): Uni<Response> = withCoroutineScope {
        val contact = pagingPolicyContactController.find(pagingPolicyContactId) ?: return@withCoroutineScope createNotFound(NOT_FOUND_MESSAGE)

        createOk(pagingPolicyContactTranslator.translate(contact))
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun listPagingPolicyContacts(first: Int?, max: Int?): Uni<Response> = withCoroutineScope {
        val contacts = pagingPolicyContactController.list(first, max)

        createOk(contacts.map { pagingPolicyContactTranslator.translate(it) })
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun updatePagingPolicyContact(
        pagingPolicyContactId: UUID,
        pagingPolicyContact: PagingPolicyContact
    ): Uni<Response> = withCoroutineScope {
        if (loggedUserId == null) {
            return@withCoroutineScope createUnauthorized(UNAUTHORIZED)
        }

        val found = pagingPolicyContactController.find(pagingPolicyContactId) ?: return@withCoroutineScope createNotFound()
        createOk(pagingPolicyContactTranslator.translate(pagingPolicyContactController.update(
            entityToUpdate = found,
            entityFromRest = pagingPolicyContact,
            modifierId = loggedUserId!!
        )))
    }
}