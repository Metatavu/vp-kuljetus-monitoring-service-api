package fi.metatavu.vp.monitoring.functional

import fi.metatavu.vp.monitoring.functional.settings.DefaultTestProfile
import fi.metatavu.vp.test.client.models.ThermalMonitor
import fi.metatavu.vp.test.client.models.ThermalMonitorStatus
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID

/**
 * Tests for thermal monitors API
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class ThermalMonitorTestsIT: AbstractFunctionalTest() {
    @Test
    fun testCreateThermalMonitor() = createTestBuilder().use {
        val thermometer1 = UUID.randomUUID()
        val thermometer2 = UUID.randomUUID()
        val activeFrom = OffsetDateTime.now()
        val activeTo = OffsetDateTime.now().plusDays(10)

        val thermalMonitor = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.ACTIVE,
            thermometerIds = arrayOf(thermometer1, thermometer2),
            lowerThresholdTemperature = -50f,
            upperThresholdTemperature = 50f,
            activeFrom = activeFrom.toString(),
            activeTo = activeTo.toString()
        )

        val created = it.manager.thermalMonitors.create(thermalMonitor)

        assertEquals("test", created.name)
        assertEquals(ThermalMonitorStatus.ACTIVE, created.status)
        assertEquals(-50f, created.lowerThresholdTemperature)
        assertEquals(50f, created.upperThresholdTemperature)
        assertEquals(activeFrom.toString().split(".")[0], OffsetDateTime.parse(created.activeFrom).atZoneSameInstant(ZoneId.systemDefault()).toString().split(".")[0])
        assertEquals(activeTo.toString().split(".")[0], OffsetDateTime.parse(created.activeTo).atZoneSameInstant(ZoneId.systemDefault()).toString().split(".")[0])
        assertEquals(2, created.thermometerIds.size)
        assertNotNull(created.thermometerIds.find { thermometer -> thermometer == thermometer1 })
        assertNotNull(created.thermometerIds.find { thermometer -> thermometer == thermometer2 })

        it.user.thermalMonitors.assertCreateFail(403, thermalMonitor)
    }

    @Test
    fun testFindThermalMonitor() = createTestBuilder().use {
        val thermalMonitor = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.ACTIVE,
            thermometerIds = arrayOf(),
        )

        val created = it.manager.thermalMonitors.create(thermalMonitor)

        val found = it.manager.thermalMonitors.findThermalMonitor(created.id!!)
        assertEquals(created.id, found.id)

        it.manager.thermalMonitors.assertFindMonitorFail(404, UUID.randomUUID())
        it.user.thermalMonitors.assertFindMonitorFail(403, created.id)
    }

    @Test
    fun testDeleteThermalMonitor() = createTestBuilder().use {
        val thermalMonitor = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.ACTIVE,
            thermometerIds = arrayOf(),
        )

        val thermalMonitor2 = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.ACTIVE,
            thermometerIds = arrayOf(),
        )

        val created = it.manager.thermalMonitors.create(thermalMonitor)
        val created2 = it.manager.thermalMonitors.create(thermalMonitor2)

        it.manager.thermalMonitors.deleteThermalMonitor(created.id!!)
        it.manager.thermalMonitors.assertFindMonitorFail(404, created.id)
        it.user.thermalMonitors.assertDeleteMonitorFail(403, created2.id!!)
    }
}