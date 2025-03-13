package fi.metatavu.vp.monitoring.functional

import fi.metatavu.vp.messaging.RoutingKey
import fi.metatavu.vp.messaging.client.MessagingClient
import fi.metatavu.vp.messaging.events.TemperatureGlobalEvent
import fi.metatavu.vp.monitoring.functional.settings.DefaultTestProfile
import fi.metatavu.vp.test.client.models.ThermalMonitor
import fi.metatavu.vp.test.client.models.ThermalMonitorIncidentStatus
import fi.metatavu.vp.test.client.models.ThermalMonitorStatus
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.*

/**
 * Tests for thermal monitor incidents API
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class ThermalMonitorIncidentsTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateThermalMonitorThresholdIncident() = createTestBuilder().use {
        val thermometerId = UUID.randomUUID()
        val thermometer2Id = UUID.randomUUID()

        val monitor = it.manager.thermalMonitors.create(
            ThermalMonitor(
                name = "Monitor",
                status = ThermalMonitorStatus.ACTIVE,
                thermometerIds = arrayOf(thermometerId, thermometer2Id),
                lowerThresholdTemperature = -50f,
                upperThresholdTemperature = 50f
            )
        )


       MessagingClient.publishMessage(
          TemperatureGlobalEvent(
               thermometerId = thermometerId,
                temperature = 20f,
            timestamp = OffsetDateTime.now().toInstant().toEpochMilli()
           ),
           routingKey = RoutingKey.TEMPERATURE
       )

        Thread.sleep(1000)
        assertEquals(0, it.manager.incidents.listThermalMonitorIncidents().size, "There should not be any incidents yet")

        val timeStamp = OffsetDateTime.now().toInstant().toEpochMilli()
        MessagingClient.publishMessage(
            TemperatureGlobalEvent(
                thermometerId = thermometerId,
                temperature = 120f,
                timestamp = timeStamp
            ),
            routingKey = RoutingKey.TEMPERATURE
        )

        Thread.sleep(1000)
        assertEquals(1, it.manager.incidents.listThermalMonitorIncidents().size, "There should be exactly one incident")

        MessagingClient.publishMessage(
            TemperatureGlobalEvent(
                thermometerId = thermometerId,
                temperature = -60f,
                timestamp = OffsetDateTime.now().toInstant().toEpochMilli()
            ),
            routingKey = RoutingKey.TEMPERATURE
        )

        Thread.sleep(1000)

        val incidents = it.manager.incidents.listThermalMonitorIncidents()
        assertEquals(1, incidents.size, "There should be exactly one incident")

        val incident = incidents.first()
        assertEquals(monitor.id, incident.monitorId, "Incident monitorId should match created monitor id")
        assertEquals(thermometerId, incident.thermometerId, "Incident thermometerId should be $thermometerId")
        assertEquals(120f, incident.temperature, "Incident temperature should be 120f")
        assertEquals(ThermalMonitorIncidentStatus.TRIGGERED, incident.status, "Incident status should be TRIGGERED")

        MessagingClient.publishMessage(
            TemperatureGlobalEvent(
                thermometerId = thermometer2Id,
                temperature = -60f,
                timestamp = OffsetDateTime.now().toInstant().toEpochMilli()
            ),
            routingKey = RoutingKey.TEMPERATURE
        )

        Thread.sleep(1000)
        val incidents2 = it.manager.incidents.listThermalMonitorIncidents()
        assertEquals(2, incidents2.size, "There should be exactly two incidents")
        val incident2 = incidents2.find { listIncident -> listIncident.thermometerId == thermometer2Id }
        assertNotNull(incident2, "Expected for incident listing to contain an incident with thermometerId $thermometer2Id")
        assertEquals(-60f, incident2!!.temperature, "Incident temperature should be -60f")
    }

    @Test
    fun testUpdateIncident() = createTestBuilder().use {
        val thermometerId = UUID.randomUUID()

        it.manager.thermalMonitors.create(
            ThermalMonitor(
                name = "Monitor",
                status = ThermalMonitorStatus.ACTIVE,
                thermometerIds = arrayOf(thermometerId),
                lowerThresholdTemperature = -50f,
                upperThresholdTemperature = 50f
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

        Thread.sleep(1000)
        val incident = it.manager.incidents.listThermalMonitorIncidents().first()

        it.user.incidents.assertUpdateFail(
            expectedStatus = 403,
            id = incident.id!!,
            incident.copy(status = ThermalMonitorIncidentStatus.ACKNOWLEDGED)
        )

        val acknowledged = it.manager.incidents.update(
            id = incident.id,
            thermalMonitorIncident = incident.copy(
                status = ThermalMonitorIncidentStatus.ACKNOWLEDGED
            )
        )

        assertEquals(ThermalMonitorIncidentStatus.ACKNOWLEDGED, acknowledged.status, "Incident status should be ACKNOWLEDGED")
        it.manager.incidents.assertUpdateFail(
            expectedStatus = 400,
            id = acknowledged.id!!,
            thermalMonitorIncident = acknowledged.copy(
                status = ThermalMonitorIncidentStatus.TRIGGERED
            )
        )

        val resolved = it.manager.incidents.update(
            id = acknowledged.id,
            thermalMonitorIncident = acknowledged.copy(
                status = ThermalMonitorIncidentStatus.RESOLVED
            )
        )

        assertEquals(ThermalMonitorIncidentStatus.RESOLVED, resolved.status, "Incident status should be RESOLVED")

        it.manager.incidents.assertUpdateFail(
            expectedStatus = 400,
            id = resolved.id!!,
            thermalMonitorIncident = resolved.copy(
                status = ThermalMonitorIncidentStatus.TRIGGERED
            )
        )

        it.manager.incidents.assertUpdateFail(
            expectedStatus = 400,
            id = resolved.id,
            thermalMonitorIncident = resolved.copy(
                status = ThermalMonitorIncidentStatus.ACKNOWLEDGED
            )
        )

        MessagingClient.publishMessage(
            TemperatureGlobalEvent(
                thermometerId = thermometerId,
                temperature = -80f,
                timestamp = OffsetDateTime.now().toInstant().toEpochMilli()
            ),
            routingKey = RoutingKey.TEMPERATURE
        )

        Thread.sleep(1000)
        val incidents = it.manager.incidents.listThermalMonitorIncidents()
        assertEquals(2, incidents.size, "There should be exactly two incidents")
        val incident2 = incidents.find { listIncident -> listIncident.temperature == -80f }
        assertNotNull(incident2, "Expected for incident listing to contain an incident with temperature -80f")
    }
}