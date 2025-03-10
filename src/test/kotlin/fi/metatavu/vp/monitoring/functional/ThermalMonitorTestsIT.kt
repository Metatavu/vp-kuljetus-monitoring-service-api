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

        assertEquals("test", created.name, "Monitor creation should return monitor with name test")
        assertEquals(ThermalMonitorStatus.ACTIVE, created.status, "Monitor creation should return monitor with status ACTIVE")
        assertEquals(-50f, created.lowerThresholdTemperature, "Monitor creation should return monitor with lowerThresholdTemperature -50f")
        assertEquals(50f, created.upperThresholdTemperature, "Monitor creation should return monitor with upperThresholdTemperature 50f")
        assertEquals(activeFrom.toString().split(".")[0], OffsetDateTime.parse(created.activeFrom).atZoneSameInstant(ZoneId.systemDefault()).toString().split(".")[0], "Monitor creation returned different activeFrom-time than what was entered")
        assertEquals(activeTo.toString().split(".")[0], OffsetDateTime.parse(created.activeTo).atZoneSameInstant(ZoneId.systemDefault()).toString().split(".")[0], "Monitor creation returned different activeTo-time than what was entered")
        assertEquals(2, created.thermometerIds.size, "Monitor creation should have returned monitor with 2 thermometerIds")
        assertNotNull(created.thermometerIds.find { thermometer -> thermometer == thermometer1 }, "Monitor creation did not return the same id for the first thermometer than what was entered")
        assertNotNull(created.thermometerIds.find { thermometer -> thermometer == thermometer2 }, "Monitor creation did not return the same id for the second thermometer than what was entered")

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
        assertEquals(created.id, found.id, "Returned id for monitor find is different than what was entered")

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

        for (i in 0..9) {
           it.manager.thermalMonitors.create(thermalMonitor)
        }

        for (i in 0..4) {
            it.manager.thermalMonitors.create(thermalMonitor2)
        }

        val monitors = it.manager.thermalMonitors.listThermalMonitors()

        assertEquals(15, monitors.size, "The listed amount of monitors is different from the amount of monitors created")

        val monitors2 = it.manager.thermalMonitors.listThermalMonitors(
            first = 3,
            max = 1000
        )

        assertEquals(12, monitors2.size, "There should be 12 monitors that fit the list filters")

        val monitors3 = it.manager.thermalMonitors.listThermalMonitors(
            first = 3,
            max = 10
        )

        assertEquals(10, monitors3.size, "There should be 10 monitors that fit the list filters")

        val monitors4 = it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.PENDING)

        assertEquals(5, monitors4.size, "There should be 5 monitors that fit the list filters")

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

        val monitors1 = it.manager.thermalMonitors.listThermalMonitors(activeAfter = time1.plusDays(1).toString(),)

        assertEquals(2, monitors1.size, "There should be 2 monitors that fit the list filters")

        val monitors2 = it.manager.thermalMonitors.listThermalMonitors(
            activeBefore = time4.minusDays(1).toString(),
            activeAfter = time1.plusDays(1).toString(),
        )

        assertEquals(1, monitors2.size, "There should be 1 monitor that fits the list filters")
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

        assertEquals("updated", updated.name, "Monitor update should have returned monitor with name 'updated'")
        assertEquals(ThermalMonitorStatus.FINISHED, updated.status, "Monitor update should have returned monitor with status FINISHED")
        assertEquals(-100f, updated.lowerThresholdTemperature, "Monitor update should have returned monitor with lowerThresholdTemperature -100f")
        assertEquals(100f, updated.upperThresholdTemperature, "Monitor update should have returned monitor with upperThresholdTemperature -100f")
        assertEquals(activeFromNew.toString().split(".")[0], OffsetDateTime.parse(updated.activeFrom).atZoneSameInstant(ZoneId.systemDefault()).toString().split(".")[0], "Monitor update returned different activeFrom-time than what was expected")
        assertEquals(activeToNew.toString().split(".")[0], OffsetDateTime.parse(updated.activeTo).atZoneSameInstant(ZoneId.systemDefault()).toString().split(".")[0], "Monitor update returned different activeTo-time than what was expected")
        assertEquals(2, updated.thermometerIds.size, "Monitor update should have returned monitor with 2 thermometers")
        assertNotNull(updated.thermometerIds.find { thermometer -> thermometer == thermometer2 }, "Updated monitor does not contain the expected monitor $thermometer2")
        assertNotNull(updated.thermometerIds.find { thermometer -> thermometer == thermometer3 }, "Updated monitor does not contain the expected monitor $thermometer3")

        it.manager.thermalMonitors.assertUpdateFail(404, UUID.randomUUID(), thermalMonitor)
        it.user.thermalMonitors.assertUpdateFail(403, updatedData.id, updatedData)
    }

    @Test
    fun testThermometerStatusResolve() = createTestBuilder().use {
        val activeFromNow = OffsetDateTime.now()
        val activeFromOneDay = OffsetDateTime.now().plusDays(1)
        val activeTo10Days = OffsetDateTime.now().plusDays(10)

        val thermalMonitor = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.PENDING,
            thermometerIds = arrayOf(),
            lowerThresholdTemperature = -50f,
            upperThresholdTemperature = 50f,
            activeFrom = activeFromNow.toString(),
            activeTo = activeTo10Days.toString()
        )

        val thermalMonitor2 = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.PENDING,
            thermometerIds = arrayOf(),
            lowerThresholdTemperature = -50f,
            upperThresholdTemperature = 50f,
            activeFrom = activeFromOneDay.toString(),
            activeTo = activeTo10Days.toString()
        )

        val monitorToActivate = it.manager.thermalMonitors.create(thermalMonitor)
        it.manager.thermalMonitors.create(thermalMonitor2)

        assertEquals(2, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.PENDING).size, "Both monitors should be PENDING at this point")
        assertEquals(0, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.ACTIVE).size, "No monitors should be ACTIVE at this point")

        it.setCronKey().thermalMonitors.resolveMonitorStatuses()

        assertEquals(1, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.PENDING).size, "Only one monitor should be PENDING at this point")
        assertEquals(1, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.ACTIVE).size, "Only one monitor should be ACTIVE at this point")

        assertEquals(ThermalMonitorStatus.ACTIVE, it.manager.thermalMonitors.findThermalMonitor(monitorToActivate.id!!).status)

        val activeFrom10DaysAgo = OffsetDateTime.now().minusDays(10)
        val activeTo1DayAgo = OffsetDateTime.now().minusDays(1)
        val thermalMonitor3 = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.ACTIVE,
            thermometerIds = arrayOf(),
            lowerThresholdTemperature = -50f,
            upperThresholdTemperature = 50f,
            activeFrom = activeFrom10DaysAgo.toString(),
            activeTo = activeTo1DayAgo.toString()
        )

        val monitorToFinish = it.manager.thermalMonitors.create(thermalMonitor3)
        assertEquals(2, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.ACTIVE).size, "There should be two ACTIVE monitors")
        assertEquals(0, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.FINISHED).size, "No monitor should be FINISHED yet")

        it.setCronKey().thermalMonitors.resolveMonitorStatuses()

        assertEquals(1, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.FINISHED).size, "There should be only one FINISHED monitor at this point")
        assertEquals(1, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.ACTIVE).size, "There should be only one ACTIVE monitor at this point")

        assertEquals(ThermalMonitorStatus.FINISHED, it.manager.thermalMonitors.findThermalMonitor(monitorToFinish.id!!).status)
    }
}