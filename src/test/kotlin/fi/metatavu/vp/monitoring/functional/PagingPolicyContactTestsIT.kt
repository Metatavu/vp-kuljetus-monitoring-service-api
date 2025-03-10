package fi.metatavu.vp.monitoring.functional

import fi.metatavu.vp.monitoring.functional.settings.DefaultTestProfile
import fi.metatavu.vp.test.client.models.PagingPolicyContact
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests for paging policy contacts API
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class PagingPolicyContactTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreatePagingPolicyContact() = createTestBuilder().use {
        val pagingPolicyContact = PagingPolicyContact(
            name = "Name",
            email = "name@domain.com"
        )

        val created = it.manager.pagingPolicyContacts.create(pagingPolicyContact)

        assertNotNull(created.id, "Created paging policy contact should have an id")
        assertEquals("Name", pagingPolicyContact.name, "Created paging policy name should be 'Name'")
        assertEquals("name@domain.com", pagingPolicyContact.email, "Created paging policy email should be 'name@domain.com'")

        it.user.pagingPolicyContacts.assertCreateFail(403, pagingPolicyContact)
    }

    @Test
    fun testFindPagingPolicyContact() = createTestBuilder().use {
        val pagingPolicyContact = PagingPolicyContact(
            name = "Name",
            email = "name@domain.com"
        )

        val created = it.manager.pagingPolicyContacts.create(pagingPolicyContact)
        val found = it.manager.pagingPolicyContacts.findPagingPolicyContact(created.id!!)

        assertEquals(created.id, found.id, "Returned id for found paging policy contact different from what was entered")
        it.user.pagingPolicyContacts.assertFindPagingPolicyContactFail(403, created.id)
        it.manager.pagingPolicyContacts.assertFindPagingPolicyContactFail(404, UUID.randomUUID())
    }

    @Test
    fun testDeletePagingPolicyContact() = createTestBuilder().use {
        val pagingPolicyContact = PagingPolicyContact(
            name = "Name",
            email = "name@domain.com"
        )

        val created = it.manager.pagingPolicyContacts.create(pagingPolicyContact)
        it.manager.pagingPolicyContacts.deletePagingPolicyContact(created.id!!)
        it.manager.pagingPolicyContacts.assertFindPagingPolicyContactFail(404, created.id)
        it.user.pagingPolicyContacts.assertDeletePagingPolicyContactFail(403, created.id)
        it.manager.pagingPolicyContacts.assertDeletePagingPolicyContactFail(404, created.id)
    }

}