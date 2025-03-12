package fi.metatavu.vp.monitoring.functional

import fi.metatavu.vp.monitoring.functional.settings.DefaultTestProfile
import fi.metatavu.vp.test.client.models.*
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests for thermal monitor paging policies API
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class ThermalMonitorPagingPoliciesTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreatePagingPolicyIT() = createTestBuilder().use {
        val contact =  it.manager.pagingPolicyContacts.create(PagingPolicyContact(
            email = "test@domain.com",
            name = "Test"
        ))

        val monitor = it.manager.thermalMonitors.create(
            ThermalMonitor(
                name = "Test",
                status = ThermalMonitorStatus.PENDING,
                thermometerIds = arrayOf()
            )
        )

        val policy = ThermalMonitorPagingPolicy(
            type = PagingPolicyType.EMAIL,
            contactId = contact.id!!,
            priority = 1,
            escalationDelaySeconds = 60,
            thermalMonitorId = monitor.id!!
        )

        val created = it.manager.thermalMonitorPagingPolicies.create(
            thermalMonitorId = monitor.id,
            policy = policy
        )

        assertEquals(PagingPolicyType.EMAIL, created.type, "Created policy type should be EMAIL")
        assertEquals(monitor.id, created.thermalMonitorId, "Created policy monitor id should be the same as entered monitor id")
        assertEquals(contact.id, created.contactId, "Created policy contact id should be the same as entered contact id")
        assertEquals(1, created.priority, "Created policy priority should be 1")
        assertEquals(60, created.escalationDelaySeconds, "Created policy escalationDelaySeconds should be 60")

        it.user.thermalMonitorPagingPolicies.assertCreateFailStatus(
            expectedStatus = 403,
            thermalMonitorId = monitor.id,
            policy =  policy
        )

        it.manager.thermalMonitorPagingPolicies.assertCreateFailStatus(
            expectedStatus = 404,
            thermalMonitorId = UUID.randomUUID(),
            policy =  policy
        )

        it.manager.thermalMonitorPagingPolicies.assertCreateFailStatus(
            expectedStatus = 400,
            thermalMonitorId = monitor.id,
            policy = policy.copy(contactId = UUID.randomUUID())
        )
    }

    @Test
    fun testFindPagingPolicyIT() = createTestBuilder().use {
        val contact = it.manager.pagingPolicyContacts.create(PagingPolicyContact(
            email = "test@domain.com",
            name = "Test"
        ))

        val monitor = it.manager.thermalMonitors.create(
            ThermalMonitor(
                name = "Test",
                status = ThermalMonitorStatus.PENDING,
                thermometerIds = arrayOf()
            )
        )

        val policy = ThermalMonitorPagingPolicy(
            type = PagingPolicyType.EMAIL,
            contactId = contact.id!!,
            priority = 1,
            escalationDelaySeconds = 60,
            thermalMonitorId = monitor.id!!
        )

        val created = it.manager.thermalMonitorPagingPolicies.create(
            thermalMonitorId = monitor.id,
            policy = policy
        )

        val found = it.manager.thermalMonitorPagingPolicies.findThermalMonitorPagingPolicy(
            thermalMonitorId = monitor.id,
            id = created.id!!
        )

        assertEquals(created.id, found.id, "Found policy id should be the same as created policy id")

        it.user.thermalMonitorPagingPolicies.assertFindFailStatus(
            expectedStatus = 403,
            thermalMonitorId = monitor.id,
            id = created.id
        )

        it.manager.thermalMonitorPagingPolicies.assertFindFailStatus(
            expectedStatus = 404,
            thermalMonitorId = UUID.randomUUID(),
            id = created.id
        )

        it.manager.thermalMonitorPagingPolicies.assertFindFailStatus(
            expectedStatus = 404,
            thermalMonitorId = monitor.id,
            id = UUID.randomUUID()
        )
    }

    @Test
    fun testDeletePagingPolicyIT() = createTestBuilder().use {
        val contact =  it.manager.pagingPolicyContacts.create(PagingPolicyContact(
            email = "test@domain.com",
            name = "Test"
        ))

        val monitor = it.manager.thermalMonitors.create(
            ThermalMonitor(
                name = "Test",
                status = ThermalMonitorStatus.PENDING,
                thermometerIds = arrayOf()
            )
        )

        val policy = ThermalMonitorPagingPolicy(
            type = PagingPolicyType.EMAIL,
            contactId = contact.id!!,
            priority = 1,
            escalationDelaySeconds = 60,
            thermalMonitorId = monitor.id!!
        )

        val created = it.manager.thermalMonitorPagingPolicies.create(
            thermalMonitorId = monitor.id,
            policy = policy
        )

        it.manager.thermalMonitorPagingPolicies.assertDeleteFailStatus(
            expectedStatus = 404,
            thermalMonitorId = UUID.randomUUID(),
            id = created.id!!
        )

        it.manager.thermalMonitorPagingPolicies.deleteThermalMonitorPagingPolicy(
            thermalMonitorId = monitor.id,
            id = created.id
        )

        it.manager.thermalMonitorPagingPolicies.assertFindFailStatus(
            expectedStatus = 404,
            thermalMonitorId = monitor.id,
            id = created.id
        )

        it.user.thermalMonitorPagingPolicies.assertDeleteFailStatus(
            expectedStatus = 403,
            thermalMonitorId = monitor.id,
            id = created.id
        )
    }

    @Test
    fun testUpdatePagingPolicyTestIT() = createTestBuilder().use {
        val contact =  it.manager.pagingPolicyContacts.create(PagingPolicyContact(
            email = "test@domain.com",
            name = "Test"
        ))

        val contact2 =  it.manager.pagingPolicyContacts.create(PagingPolicyContact(
            email = "testi@domain.fi",
            name = "Testi"
        ))

        val monitor = it.manager.thermalMonitors.create(
            ThermalMonitor(
                name = "Test",
                status = ThermalMonitorStatus.PENDING,
                thermometerIds = arrayOf()
            )
        )

        val policy = ThermalMonitorPagingPolicy(
            type = PagingPolicyType.EMAIL,
            contactId = contact.id!!,
            priority = 1,
            escalationDelaySeconds = 60,
            thermalMonitorId = monitor.id!!
        )

        val created = it.manager.thermalMonitorPagingPolicies.create(
            thermalMonitorId = monitor.id,
            policy = policy
        )

        val updated = it.manager.thermalMonitorPagingPolicies.update(
            thermalMonitorId = monitor.id,
            id = created.id!!,
            thermalMonitorPagingPolicy = policy.copy(
                priority = 2,
                escalationDelaySeconds = 120,
                contactId = contact2.id!!
            )
        )

        assertEquals(2, updated.priority, "Updated policy priority should be 2")
        assertEquals(120, updated.escalationDelaySeconds, "Updated policy escalationDelaySeconds should be 120")
        assertEquals(contact2.id, updated.contactId, "Updated policy contact id should be the same as entered contact id")

        it.user.thermalMonitorPagingPolicies.assertUpdateFailStatus(
            expectedStatus = 403,
            thermalMonitorId = monitor.id,
            id = created.id,
            thermalMonitorPagingPolicy = created
        )

        it.manager.thermalMonitorPagingPolicies.assertUpdateFailStatus(
            expectedStatus = 404,
            thermalMonitorId = UUID.randomUUID(),
            id = created.id,
            thermalMonitorPagingPolicy = policy.copy(
                priority = 2,
                escalationDelaySeconds = 120,
                contactId = contact2.id
            )
        )

        it.manager.thermalMonitorPagingPolicies.assertUpdateFailStatus(
            expectedStatus = 404,
            thermalMonitorId = monitor.id,
            id = UUID.randomUUID(),
            thermalMonitorPagingPolicy = policy.copy(
                priority = 2,
                escalationDelaySeconds = 120,
                contactId = contact2.id
            )
        )

        it.manager.thermalMonitorPagingPolicies.assertUpdateFailStatus(
            expectedStatus = 400,
            thermalMonitorId = monitor.id,
            id = created.id,
            thermalMonitorPagingPolicy = policy.copy(
                priority = 2,
                escalationDelaySeconds = 120,
                contactId = UUID.randomUUID()
            )
        )
    }

    @Test
    fun testListPagingPoliciesIT() = createTestBuilder().use {
        val contact =  it.manager.pagingPolicyContacts.create(PagingPolicyContact(
            email = "test@domain.com",
            name = "Test"
        ))

        val monitor = it.manager.thermalMonitors.create(
            ThermalMonitor(
                name = "Test",
                status = ThermalMonitorStatus.PENDING,
                thermometerIds = arrayOf()
            )
        )

        val policy = ThermalMonitorPagingPolicy(
            type = PagingPolicyType.EMAIL,
            contactId = contact.id!!,
            priority = 1,
            escalationDelaySeconds = 60,
            thermalMonitorId = monitor.id!!
        )

        for (i in 0..14) {
            it.manager.thermalMonitorPagingPolicies.create(
                thermalMonitorId = monitor.id,
                policy = policy
            )
        }

        assertEquals(
            15,
            it.manager.thermalMonitorPagingPolicies.listThermalMonitorPagingPolicies(thermalMonitorId = monitor.id, first = 0, max = 100).size,
            "Listing should return 15 policies when first = 0 and max = 100"
        )

        assertEquals(
            13,
            it.manager.thermalMonitorPagingPolicies.listThermalMonitorPagingPolicies(thermalMonitorId = monitor.id, first = 2, max = 15).size,
            "Listing should return 13 policies when first = 2 and max = 15"
        )

        assertEquals(
            8,
            it.manager.thermalMonitorPagingPolicies.listThermalMonitorPagingPolicies(thermalMonitorId = monitor.id, first = 2, max = 8).size,
            "Listing should return 8 policies when first = 2 and max = 8"
        )

        assertEquals(
            4,
            it.manager.thermalMonitorPagingPolicies.listThermalMonitorPagingPolicies(thermalMonitorId = monitor.id, first = 11, max = 8).size,
            "Listing should return 4 policies when first = 11 and max = 8"
        )

        it.user.thermalMonitorPagingPolicies.assertListFailStatus(403, monitor.id, 0, 100)
        it.manager.thermalMonitorPagingPolicies.assertListFailStatus(404, UUID.randomUUID(), 0, 100)

        val monitor2 = it.manager.thermalMonitors.create(
            ThermalMonitor(
                name = "Test",
                status = ThermalMonitorStatus.PENDING,
                thermometerIds = arrayOf()
            )
        )

        val policy2 = ThermalMonitorPagingPolicy(
            type = PagingPolicyType.EMAIL,
            contactId = contact.id,
            priority = 1,
            escalationDelaySeconds = 60,
            thermalMonitorId = monitor2.id!!
        )

        it.manager.thermalMonitorPagingPolicies.create(
            thermalMonitorId = monitor2.id,
            policy = policy2
        )

        assertEquals(1, it.manager.thermalMonitorPagingPolicies.listThermalMonitorPagingPolicies(thermalMonitorId = monitor2.id, first = 0, max = 100).size, "Listing should return 1 policy when monitor is monitor2, first = 0 and max = 100")
    }
}