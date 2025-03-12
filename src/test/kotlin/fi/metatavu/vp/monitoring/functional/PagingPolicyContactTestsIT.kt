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

    @Test
    fun testUpdatePagingPolicyContact() = createTestBuilder().use {
        val pagingPolicyContact = PagingPolicyContact(
            name = "Name",
            email = "name@domain.com"
        )

        val created = it.manager.pagingPolicyContacts.create(pagingPolicyContact)
        val newData = created.copy(name = "Nimi", email = "nimi@osoite.fi")
        val updated = it.manager.pagingPolicyContacts.update(created.id!!, newData)

        assertEquals(created.id, updated.id, "Id should stay the same after paging policy contact entity has been updated")
        assertEquals("Nimi", updated.name, "The updated paging policy contact name should be 'Nimi'")
        assertEquals("nimi@osoite.fi", updated.email, "The updated paging policy contact email should be 'nimi@osoite.fi'")

        it.user.pagingPolicyContacts.assertUpdateFail(403, created.id, newData)
        it.manager.pagingPolicyContacts.assertUpdateFail(404, UUID.randomUUID(), newData)
    }

    @Test
    fun testListPagingPolicyContacts() = createTestBuilder().use {
        val pagingPolicyContact = PagingPolicyContact(
            name = "Name",
            email = "name@domain.com"
        )

        for (i in 0..14) {
            it.manager.pagingPolicyContacts.create(pagingPolicyContact)
        }

        assertEquals(15, it.manager.pagingPolicyContacts.listPagingPolicyContacts(
            first = null,
            max = null
        ).size, "There should be 15 paging policy contacts when listing with first=null, max=null")

        assertEquals(13, it.manager.pagingPolicyContacts.listPagingPolicyContacts(
            first = 2,
            max = 100
        ).size, "There should be 13 paging policy contacts when listing with first=2, max=100")

        assertEquals(9, it.manager.pagingPolicyContacts.listPagingPolicyContacts(
            first = 2,
            max = 9
        ).size, "There should be 9 paging policy contacts when listing with first=2, max=9")

        assertEquals(6, it.manager.pagingPolicyContacts.listPagingPolicyContacts(
            first = 9,
            max = 10
        ).size, "There should be 6 paging policy contacts when listing with first=9, max=10")

        it.user.pagingPolicyContacts.assertListPagingPolicyContactsFail(403)
    }
}