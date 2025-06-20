package fi.metatavu.vp.monitoring.functional

import fi.metatavu.vp.messaging.RoutingKey
import fi.metatavu.vp.messaging.client.MessagingClient
import fi.metatavu.vp.messaging.events.TemperatureGlobalEvent
import fi.metatavu.vp.monitoring.functional.resources.*
import fi.metatavu.vp.monitoring.functional.settings.ApiTestSettings
import fi.metatavu.vp.monitoring.functional.settings.DefaultTestProfile
import fi.metatavu.vp.test.client.models.*
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.testcontainers.shaded.org.awaitility.Awaitility
import java.time.Duration
import java.time.OffsetDateTime
import java.util.*

/**
 * Tests for incident escalations
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(SitesTestResource::class),
    QuarkusTestResource(TerminalThermometersTestResource::class),
    QuarkusTestResource(TruckOrTowablelThermometersTestResource::class),
    QuarkusTestResource(TrucksTestResource::class),
    QuarkusTestResource(TowablesTestResource::class),
    QuarkusTestResource(MailgunTestResource::class),

)
@TestProfile(DefaultTestProfile::class)
class EscalationTestsIT: AbstractFunctionalTest() {
    @Test
    fun testCreatePagedPolicy() = createTestBuilder().use {
        val thermometerId = UUID.randomUUID()

        val monitor = it.manager.thermalMonitors.create(
            ThermalMonitor(
                name = "Monitor",
                status = ThermalMonitorStatus.ACTIVE,
                thermometerIds = arrayOf(thermometerId),
                lowerThresholdTemperature = -50f,
                upperThresholdTemperature = 50f,
                monitorType = ThermalMonitorType.ONE_OFF
            )
        )

        val policyContact = it.manager.pagingPolicyContacts.create(
            PagingPolicyContact(
                name = "Name",
                contact = "test@example.com",
                type = PagingPolicyType.EMAIL
            )
        )

        val policy = it.manager.thermalMonitorPagingPolicies.create(
            thermalMonitorId = monitor.id!!,
            ThermalMonitorPagingPolicy(
                contactId = policyContact.id!!,
                escalationDelaySeconds = 0,
                priority = 1,
                thermalMonitorId = monitor.id
            )
        )

        MessagingClient.publishMessage(
            TemperatureGlobalEvent(
                thermometerId = thermometerId,
                temperature = 60f,
                timestamp = OffsetDateTime.now().toInstant().toEpochMilli()
            ),
            routingKey = RoutingKey.TEMPERATURE
        )

        Awaitility.await().atMost(Duration.ofMinutes(2)).until {
            it.manager.incidents.listThermalMonitorIncidents().size == 1
        }

        assertEquals(
            0,
            it.manager.incidents.listThermalMonitorIncidents().first().pagedPolicies!!.size,
            "No paged policies should be created before the cron endpoint is triggered")
        it.setCronKey().thermalMonitorPagingPolicies.triggerPolicies()

        Awaitility.await().atMost(Duration.ofMinutes(2)).until {
            it.manager.incidents.listThermalMonitorIncidents().firstOrNull()?.pagedPolicies?.size == 1
        }

        val incident = it.manager.incidents.listThermalMonitorIncidents().first()

        assertEquals(
            1,
            incident.pagedPolicies!!.size,
            "There should be one paged policy after the cron endpoint is triggered"
        )

        val pagedPolicy = incident.pagedPolicies.first()
        assertEquals(policy.id, pagedPolicy.policyId, "Paged policy policyId should match the created policy")
        assertEquals(policyContact.id, pagedPolicy.contactId, "Paged policy contactId should match the created contact")
        assertEquals(incident.id, pagedPolicy.incidentId, "Paged policy incidentId should match the created incident")
        assertNotNull(pagedPolicy.time, "Paged policy should have a timestamp")
    }

    @Test
    fun testEscalationDelay() = createTestBuilder().use {
        val thermometerId = UUID.randomUUID()

        val monitor = it.manager.thermalMonitors.create(
            ThermalMonitor(
                name = "Monitor",
                status = ThermalMonitorStatus.ACTIVE,
                thermometerIds = arrayOf(thermometerId),
                lowerThresholdTemperature = -50f,
                upperThresholdTemperature = 50f,
                monitorType = ThermalMonitorType.ONE_OFF
            )
        )

        val policyContact = it.manager.pagingPolicyContacts.create(
            PagingPolicyContact(
                name = "Name",
                contact = "test@example.com",
                type = PagingPolicyType.EMAIL
            )
        )

        val policy = it.manager.thermalMonitorPagingPolicies.create(
            thermalMonitorId = monitor.id!!,
            ThermalMonitorPagingPolicy(
                contactId = policyContact.id!!,
                escalationDelaySeconds = 0,
                priority = 1,
                thermalMonitorId = monitor.id
            )
        )

        val policy2 = it.manager.thermalMonitorPagingPolicies.create(
            thermalMonitorId = monitor.id,
            ThermalMonitorPagingPolicy(
                contactId = policyContact.id,
                escalationDelaySeconds = 10,
                priority = 1,
                thermalMonitorId = monitor.id
            )
        )

        MessagingClient.publishMessage(
            TemperatureGlobalEvent(
                thermometerId = thermometerId,
                temperature = 60f,
                timestamp = OffsetDateTime.now().toInstant().toEpochMilli()
            ),
            routingKey = RoutingKey.TEMPERATURE
        )

        Awaitility.await().atMost(Duration.ofMinutes(2)).until {
            it.setCronKey().thermalMonitorPagingPolicies.triggerPolicies()
            it.manager.incidents.listThermalMonitorIncidents().firstOrNull()?.pagedPolicies?.size == 1
        }

        assertEquals(
            policy.id,
            it.manager.incidents.listThermalMonitorIncidents().first().pagedPolicies!!.first().policyId,
            "Triggered policy id should match the first policy")

        Awaitility.await().atMost(Duration.ofMinutes(2)).until {
            it.setCronKey().thermalMonitorPagingPolicies.triggerPolicies()
            it.manager.incidents.listThermalMonitorIncidents().firstOrNull()?.pagedPolicies?.size == 2
        }

        assertEquals(
            policy2.id,
            it.manager.incidents.listThermalMonitorIncidents().first().pagedPolicies!!.first().policyId,
            "At this point, the second created policy should be the first in the list")
    }

    @Test
    fun testStopEscalation() = createTestBuilder().use {
        val thermometerId = UUID.randomUUID()

        val monitor = it.manager.thermalMonitors.create(
            ThermalMonitor(
                name = "Monitor",
                status = ThermalMonitorStatus.ACTIVE,
                thermometerIds = arrayOf(thermometerId),
                lowerThresholdTemperature = -50f,
                upperThresholdTemperature = 50f,
                monitorType = ThermalMonitorType.ONE_OFF
            )
        )

        val policyContact = it.manager.pagingPolicyContacts.create(
            PagingPolicyContact(
                name = "Name",
                contact = "test@example.com",
                type = PagingPolicyType.EMAIL
            )
        )

        val policy = it.manager.thermalMonitorPagingPolicies.create(
            thermalMonitorId = monitor.id!!,
            ThermalMonitorPagingPolicy(
                contactId = policyContact.id!!,
                escalationDelaySeconds = 0,
                priority = 1,
                thermalMonitorId = monitor.id
            )
        )

        it.manager.thermalMonitorPagingPolicies.create(
            thermalMonitorId = monitor.id,
            ThermalMonitorPagingPolicy(
                contactId = policyContact.id,
                escalationDelaySeconds = 5,
                priority = 1,
                thermalMonitorId = monitor.id
            )
        )

        MessagingClient.publishMessage(
            TemperatureGlobalEvent(
                thermometerId = thermometerId,
                temperature = 60f,
                timestamp = OffsetDateTime.now().toInstant().toEpochMilli()
            ),
            routingKey = RoutingKey.TEMPERATURE
        )

        Awaitility.await().atMost(Duration.ofMinutes(2)).until {
            it.setCronKey().thermalMonitorPagingPolicies.triggerPolicies()
            it.manager.incidents.listThermalMonitorIncidents().firstOrNull()?.pagedPolicies?.size == 1
        }

        val incident = it.manager.incidents.listThermalMonitorIncidents().first()
        it.manager.incidents.update(
            id = incident.id!!,
            thermalMonitorIncident = incident.copy(
                status = ThermalMonitorIncidentStatus.ACKNOWLEDGED
            )
        )

        Thread.sleep(5000)
        it.setCronKey().thermalMonitorPagingPolicies.triggerPolicies()

        Awaitility.await().atMost(Duration.ofMinutes(2)).until {
            it.manager.incidents.listThermalMonitorIncidents().firstOrNull()?.pagedPolicies?.size == 1
        }
        val pagedPolicies = it.manager.incidents.listThermalMonitorIncidents().firstOrNull()?.pagedPolicies
        assertEquals(1, pagedPolicies!!.size, "New policies should not be created after incident is acknowledged")
        assertEquals(policy.id!!, pagedPolicies.first().policyId, "Triggered policy id should match the first policy")
    }

    @Test
    fun testSendThresholdHighIncidentEmail() = createTestBuilder().use {
        val thermometerId = TestData.terminalThermometers[0].id!!

        val monitor = it.manager.thermalMonitors.create(
            ThermalMonitor(
                name = "Monitor 1",
                status = ThermalMonitorStatus.ACTIVE,
                thermometerIds = arrayOf(thermometerId),
                lowerThresholdTemperature = -50f,
                upperThresholdTemperature = 50f,
                monitorType = ThermalMonitorType.ONE_OFF
            )
        )

        val policyContact = it.manager.pagingPolicyContacts.create(
            PagingPolicyContact(
                name = "Name",
                contact = "test@example.com",
                type = PagingPolicyType.EMAIL
            )
        )

        it.manager.thermalMonitorPagingPolicies.create(
            thermalMonitorId = monitor.id!!,
            ThermalMonitorPagingPolicy(
                contactId = policyContact.id!!,
                escalationDelaySeconds = 0,
                priority = 1,
                thermalMonitorId = monitor.id
            )
        )

        MessagingClient.publishMessage(
            TemperatureGlobalEvent(
                thermometerId = thermometerId,
                temperature = 60f,
                timestamp = OffsetDateTime.now().toInstant().toEpochMilli()
            ),
            routingKey = RoutingKey.TEMPERATURE
        )


        Awaitility.await().atMost(Duration.ofMinutes(2)).until {
            it.setCronKey().thermalMonitorPagingPolicies.triggerPolicies()
            it.manager.incidents.listThermalMonitorIncidents().firstOrNull()?.pagedPolicies?.size == 1
        }

        val mailgunMocker = MailgunMocker()
        val incident = it.manager.incidents.listThermalMonitorIncidents().first()
        val expectedContent = "HÄLYTYKSEN TIEDOT\n\n"
            .plus("KOHDE: terminaali Test site\n")
            .plus("VAHTI: ${monitor.name}\n")
            .plus("ANTURI: target thermometer\n")
            .plus("SYY: Lämpötila on 60.0 °C, joka on korkeampi kuin asetettu yläraja: 50.0 °C\n")
            .plus("AIKA: ${incident.timestamp}")
        val emailParameters = mailgunMocker.createParameterList(
            fromEmail = ApiTestSettings.MAILGUN_SENDER_EMAIL,
            to = "test@example.com",
            subject = "LÄMPÖTILAHÄLYTYS terminaalissa Test site",
            content = expectedContent
        )

        mailgunMocker.verifyMessageSent(emailParameters)
    }

    @Test
    fun testSendThresholdLowIncidentEmail() = createTestBuilder().use {
        val thermometerId = TestData.truckAndTowableThermometers[0].id!!

        val monitor = it.manager.thermalMonitors.create(
            ThermalMonitor(
                name = "Monitori",
                status = ThermalMonitorStatus.ACTIVE,
                thermometerIds = arrayOf(thermometerId),
                lowerThresholdTemperature = -50f,
                upperThresholdTemperature = 50f,
                monitorType = ThermalMonitorType.ONE_OFF
            )
        )

        val policyContact = it.manager.pagingPolicyContacts.create(
            PagingPolicyContact(
                name = "Nimi",
                contact = "testi@testi.fi",
                type = PagingPolicyType.EMAIL
            )
        )

        it.manager.thermalMonitorPagingPolicies.create(
            thermalMonitorId = monitor.id!!,
            ThermalMonitorPagingPolicy(
                contactId = policyContact.id!!,
                escalationDelaySeconds = 0,
                priority = 1,
                thermalMonitorId = monitor.id
            )
        )

        MessagingClient.publishMessage(
            TemperatureGlobalEvent(
                thermometerId = thermometerId,
                temperature = -100f,
                timestamp = OffsetDateTime.now().toInstant().toEpochMilli()
            ),
            routingKey = RoutingKey.TEMPERATURE
        )


        Awaitility.await().atMost(Duration.ofMinutes(2)).until {
            it.setCronKey().thermalMonitorPagingPolicies.triggerPolicies()
            it.manager.incidents.listThermalMonitorIncidents().firstOrNull()?.pagedPolicies?.size == 1
        }

        val mailgunMocker = MailgunMocker()
        val incident = it.manager.incidents.listThermalMonitorIncidents().first()
        val expectedContent = "HÄLYTYKSEN TIEDOT\n\n"
            .plus("KOHDE: ajoneuvo Roadkiller\n")
            .plus("VAHTI: ${monitor.name}\n")
            .plus("ANTURI: Truck thermometer 1\n")
            .plus("SYY: Lämpötila on -100.0 °C, joka on alhaisempi kuin asetettu alaraja: -50.0 °C\n")
            .plus("AIKA: ${incident.timestamp}")
        val emailParameters = mailgunMocker.createParameterList(
            fromEmail = ApiTestSettings.MAILGUN_SENDER_EMAIL,
            to = "testi@testi.fi",
            subject = "LÄMPÖTILAHÄLYTYS ajoneuvossa Roadkiller",
            content = expectedContent
        )

        mailgunMocker.verifyMessageSent(emailParameters)
    }

    @Test
    fun testSendSensorLostIncidentEmail() = createTestBuilder().use {
        val thermometerId = TestData.truckAndTowableThermometers[1].id!!

        val monitor = it.manager.thermalMonitors.create(
            ThermalMonitor(
                name = "Monitori",
                status = ThermalMonitorStatus.ACTIVE,
                thermometerIds = arrayOf(thermometerId),
                lowerThresholdTemperature = -50f,
                upperThresholdTemperature = 50f,
                monitorType = ThermalMonitorType.ONE_OFF
            )
        )

        val policyContact = it.manager.pagingPolicyContacts.create(
            PagingPolicyContact(
                name = "Nimi",
                contact = "testi@testi.fi",
                type = PagingPolicyType.EMAIL
            )
        )

        it.manager.thermalMonitorPagingPolicies.create(
            thermalMonitorId = monitor.id!!,
            ThermalMonitorPagingPolicy(
                contactId = policyContact.id!!,
                escalationDelaySeconds = 0,
                priority = 1,
                thermalMonitorId = monitor.id
            )
        )

        MessagingClient.publishMessage(
            TemperatureGlobalEvent(
                thermometerId = thermometerId,
                temperature = 30f,
                timestamp = OffsetDateTime.now().minusMinutes(10).toEpochSecond()
            ),
            routingKey = RoutingKey.TEMPERATURE
        )


        Awaitility.await().atMost(Duration.ofMinutes(2)).until {
            it.setCronKey().incidents.createSensorLostIncidents()
            it.setCronKey().thermalMonitorPagingPolicies.triggerPolicies()
            it.manager.incidents.listThermalMonitorIncidents().firstOrNull()?.pagedPolicies?.size == 1
        }

        val mailgunMocker = MailgunMocker()

        val incident = it.manager.incidents.listThermalMonitorIncidents().first()

        val expectedContent = "HÄLYTYKSEN TIEDOT\n\n"
            .plus("KOHDE: perävaunu Raahattava\n")
            .plus("VAHTI: ${monitor.name}\n")
            .plus("ANTURI: Raahattava lämpömittari\n")
            .plus("SYY: Lämpötila ei päivittynyt määräajassa. Järjestelmässä asetettu raja on 5 minuuttia.\n")
            .plus("AIKA: ${incident.timestamp}")
        val emailParameters = mailgunMocker.createParameterList(
            fromEmail = ApiTestSettings.MAILGUN_SENDER_EMAIL,
            to = "testi@testi.fi",
            subject = "LÄMPÖTILAHÄLYTYS perävaunussa Raahattava",
            content = expectedContent
        )

        mailgunMocker.verifyMessageSent(emailParameters)
    }
}