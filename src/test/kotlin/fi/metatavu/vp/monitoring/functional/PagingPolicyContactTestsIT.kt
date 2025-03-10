package fi.metatavu.vp.monitoring.functional

import fi.metatavu.vp.monitoring.functional.settings.DefaultTestProfile
import fi.metatavu.vp.test.client.models.PagingPolicyContact
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
        assertEquals("name@domain.con", pagingPolicyContact.email, "Created paging policy email should be 'Email'")
    }
}