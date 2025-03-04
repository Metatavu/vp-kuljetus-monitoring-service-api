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

    @Test
    fun testListThermalMonitors() = createTestBuilder().use {
        val thermalMonitor = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.ACTIVE,
            thermometerIds = arrayOf(),
        )

        val thermalMonitor2 = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.PENDING,
            thermometerIds = arrayOf(),
        )

        var i = 0
        while (i < 10) {
           it.manager.thermalMonitors.create(thermalMonitor)
            i++
        }

        i = 0
        while (i < 5) {
            it.manager.thermalMonitors.create(thermalMonitor2)
            i++
        }

        val monitors = it.manager.thermalMonitors.listThermalMonitors(
            null,
            null,
            null,
            null,
            null
        )

        assertEquals(15, monitors.size)

        val monitors2 = it.manager.thermalMonitors.listThermalMonitors(
            null,
            null,
            null,
            first = 3,
            max = 1000
        )

        assertEquals(12, monitors2.size)

        val monitors3 = it.manager.thermalMonitors.listThermalMonitors(
            null,
            null,
            null,
            first = 3,
            max = 10
        )

        assertEquals(10, monitors3.size)

        val monitors4 = it.manager.thermalMonitors.listThermalMonitors(
            ThermalMonitorStatus.PENDING,
            null,
            null,
            null,
            null
        )

        assertEquals(5, monitors4.size)

        it.user.thermalMonitors.assertListMonitorFail(403)
    }

    @Test
    fun testThermalMonitorListTimeFilters() = createTestBuilder().use {
        val time1 = OffsetDateTime.now().plusDays(2)
        val time2 = OffsetDateTime.now().plusDays(10)
        val time3 = OffsetDateTime.now().plusDays(15)
        val time4 = OffsetDateTime.now().plusDays(20)

        val thermalMonitor = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.ACTIVE,
            thermometerIds = arrayOf(),
            activeFrom = time1.toString(),
            activeTo = time2.toString()
        )

        val thermalMonitor2 = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.PENDING,
            thermometerIds = arrayOf(),
            activeFrom = time2.toString(),
            activeTo = time3.toString()
        )

        val thermalMonitor3 = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.PENDING,
            thermometerIds = arrayOf(),
            activeFrom = time3.toString(),
            activeTo = time4.toString()
        )

        it.manager.thermalMonitors.create(thermalMonitor)
        it.manager.thermalMonitors.create(thermalMonitor2)
        it.manager.thermalMonitors.create(thermalMonitor3)

        val monitors1 = it.manager.thermalMonitors.listThermalMonitors(
            null,
            null,
            time1.plusDays(1).toString(),
            null,
            null
        )

        assertEquals(2, monitors1.size)

        val monitors2 = it.manager.thermalMonitors.listThermalMonitors(
            null,
            time4.minusDays(1).toString(),
            time1.plusDays(1).toString(),
            null,
            null
        )

        assertEquals(1, monitors2.size)
    }

    @Test
    fun testThermometerUpdate() = createTestBuilder().use {
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

        val thermometer3 = UUID.randomUUID()
        val activeFromNew = OffsetDateTime.now().plusDays(5)
        val activeToNew = OffsetDateTime.now().plusDays(15)

        val updatedData = created.copy(
            name = "updated",
            status = ThermalMonitorStatus.FINISHED,
            thermometerIds = arrayOf(thermometer2, thermometer2, thermometer3),
            lowerThresholdTemperature = -100f,
            upperThresholdTemperature = 100f,
            activeFrom = activeFromNew.toString(),
            activeTo = activeToNew.toString()
        )

        val updated = it.manager.thermalMonitors.update(updatedData.id!!, updatedData)

        assertEquals("updated", updated.name)
        assertEquals(ThermalMonitorStatus.FINISHED, updated.status)
        assertEquals(-100f, updated.lowerThresholdTemperature)
        assertEquals(100f, updated.upperThresholdTemperature)
        assertEquals(activeFromNew.toString().split(".")[0], OffsetDateTime.parse(updated.activeFrom).atZoneSameInstant(ZoneId.systemDefault()).toString().split(".")[0])
        assertEquals(activeToNew.toString().split(".")[0], OffsetDateTime.parse(updated.activeTo).atZoneSameInstant(ZoneId.systemDefault()).toString().split(".")[0])
        assertEquals(2, updated.thermometerIds.size)
        assertNotNull(updated.thermometerIds.find { thermometer -> thermometer == thermometer2 })
        assertNotNull(updated.thermometerIds.find { thermometer -> thermometer == thermometer3 })

        it.manager.thermalMonitors.assertUpdateFail(404, UUID.randomUUID(), thermalMonitor)
        it.user.thermalMonitors.assertUpdateFail(403, updatedData.id, updatedData)
    }
}