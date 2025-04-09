package fi.metatavu.vp.monitoring.functional

import fi.metatavu.vp.monitoring.functional.settings.DefaultTestProfile
import fi.metatavu.vp.test.client.models.*
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
        val activeTo = OffsetDateTime.now().plusHours(10)

        val thermalMonitor = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.ACTIVE,
            thermometerIds = arrayOf(thermometer1, thermometer2),
            lowerThresholdTemperature = -50f,
            upperThresholdTemperature = 50f,
            monitorType = ThermalMonitorType.ONE_OFF,
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
        assertEquals(ThermalMonitorType.ONE_OFF, created.monitorType, "Monitor creation should return monitor with monitorType ONE_OFF")
        it.user.thermalMonitors.assertCreateFail(403, thermalMonitor)

        it.manager.thermalMonitors.assertCreateFail(
            expectedStatus = 400,
            thermalMonitor = ThermalMonitor(
                name = "Test",
                status = ThermalMonitorStatus.ACTIVE,
                thermometerIds = arrayOf(),
                monitorType = ThermalMonitorType.ONE_OFF,
                schedule = arrayOf()
            )
        )

        it.manager.thermalMonitors.assertCreateFail(
            expectedStatus = 400,
            thermalMonitor = ThermalMonitor(
                name = "Test",
                status = ThermalMonitorStatus.ACTIVE,
                thermometerIds = arrayOf(),
                monitorType = ThermalMonitorType.SCHEDULED,
                schedule = null
            )
        )

        it.manager.thermalMonitors.assertCreateFail(
            expectedStatus = 400,
            thermalMonitor = thermalMonitor.copy(
                status = ThermalMonitorStatus.INACTIVE
            )
        )
    }

    @Test
    fun testFindThermalMonitor() = createTestBuilder().use {
        val thermalMonitor = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.ACTIVE,
            thermometerIds = arrayOf(),
            monitorType = ThermalMonitorType.ONE_OFF
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
            monitorType = ThermalMonitorType.ONE_OFF
        )

        val thermalMonitor2 = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.ACTIVE,
            thermometerIds = arrayOf(),
            monitorType = ThermalMonitorType.ONE_OFF
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
            monitorType = ThermalMonitorType.ONE_OFF
        )

        val thermalMonitor2 = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.PENDING,
            thermometerIds = arrayOf(),
            monitorType = ThermalMonitorType.ONE_OFF
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
        val time1 = OffsetDateTime.now().plusHours(2)
        val time2 = OffsetDateTime.now().plusHours(10)
        val time3 = OffsetDateTime.now().plusHours(15)
        val time4 = OffsetDateTime.now().plusHours(20)

        val thermalMonitor = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.ACTIVE,
            thermometerIds = arrayOf(),
            activeFrom = time1.toString(),
            monitorType = ThermalMonitorType.ONE_OFF,
            activeTo = time2.toString()
        )

        val thermalMonitor2 = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.PENDING,
            thermometerIds = arrayOf(),
            activeFrom = time2.toString(),
            monitorType = ThermalMonitorType.ONE_OFF,
            activeTo = time3.toString()
        )

        val thermalMonitor3 = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.PENDING,
            thermometerIds = arrayOf(),
            activeFrom = time3.toString(),
            monitorType = ThermalMonitorType.ONE_OFF,
            activeTo = time4.toString()
        )

        it.manager.thermalMonitors.create(thermalMonitor)
        it.manager.thermalMonitors.create(thermalMonitor2)
        it.manager.thermalMonitors.create(thermalMonitor3)

        val monitors1 = it.manager.thermalMonitors.listThermalMonitors(activeAfter = time1.plusHours(1).toString(),)

        assertEquals(2, monitors1.size, "There should be 2 monitors that fit the list filters")

        val monitors2 = it.manager.thermalMonitors.listThermalMonitors(
            activeBefore = time4.minusHours(1).toString(),
            activeAfter = time1.plusHours(1).toString(),
        )

        assertEquals(1, monitors2.size, "There should be 1 monitor that fits the list filters")
    }

    @Test
    fun testThermometerUpdate() = createTestBuilder().use {
        val thermometer1 = UUID.randomUUID()
        val thermometer2 = UUID.randomUUID()
        val activeFrom = OffsetDateTime.now()
        val activeTo = OffsetDateTime.now().plusHours(1)

        val thermalMonitor = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.ACTIVE,
            thermometerIds = arrayOf(thermometer1, thermometer2),
            lowerThresholdTemperature = -50f,
            upperThresholdTemperature = 50f,
            activeFrom = activeFrom.toString(),
            monitorType = ThermalMonitorType.ONE_OFF,
            activeTo = activeTo.toString()
        )

        val created = it.manager.thermalMonitors.create(thermalMonitor)

        val thermometer3 = UUID.randomUUID()
        val activeFromNew = OffsetDateTime.now().plusHours(1)
        val activeToNew = OffsetDateTime.now().plusHours(2)

        val updatedData = created.copy(
            name = "updated",
            status = ThermalMonitorStatus.FINISHED,
            thermometerIds = arrayOf(thermometer2, thermometer2, thermometer3),
            lowerThresholdTemperature = -100f,
            upperThresholdTemperature = 100f,
            activeFrom = activeFromNew.toString(),
            monitorType = ThermalMonitorType.ONE_OFF,
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

        it.manager.thermalMonitors.assertUpdateFail(
            expectedStatus = 400,
            id = updated.id!!,
            thermalMonitor = updated.copy(
                schedule = arrayOf(
                    ThermalMonitorSchedulePeriod(
                        start = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.MONDAY,
                            hour = 10,
                            minute = 0
                        ),
                        end = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.FRIDAY,
                            hour = 20,
                            minute = 6
                        )
                    )
                )
            )
        )

        it.manager.thermalMonitors.assertCreateFail(
            expectedStatus = 400,
            thermalMonitor = updated.copy(
                status = ThermalMonitorStatus.INACTIVE
            )
        )
    }

    @Test
    fun testOneOffMonitorStatusResolve() = createTestBuilder().use {
        val activeFromNow = OffsetDateTime.now()
        val activeFromOneHour = OffsetDateTime.now().plusHours(1)
        val activeTo10Hours = OffsetDateTime.now().plusHours(10)

        val thermalMonitor = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.PENDING,
            thermometerIds = arrayOf(),
            lowerThresholdTemperature = -50f,
            upperThresholdTemperature = 50f,
            activeFrom = activeFromNow.toString(),
            monitorType = ThermalMonitorType.ONE_OFF,
            activeTo = activeTo10Hours.toString()
        )

        val thermalMonitor2 = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.PENDING,
            thermometerIds = arrayOf(),
            lowerThresholdTemperature = -50f,
            upperThresholdTemperature = 50f,
            activeFrom = activeFromOneHour.toString(),
            monitorType = ThermalMonitorType.ONE_OFF,
            activeTo = activeTo10Hours.toString()
        )

        val monitorToActivate = it.manager.thermalMonitors.create(thermalMonitor)
        it.manager.thermalMonitors.create(thermalMonitor2)

        assertEquals(2, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.PENDING).size, "Both monitors should be PENDING at this point")
        assertEquals(0, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.ACTIVE).size, "No monitors should be ACTIVE at this point")

        it.setCronKey().thermalMonitors.resolveMonitorStatuses()

        assertEquals(1, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.PENDING).size, "Only one monitor should be PENDING at this point")
        assertEquals(1, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.ACTIVE).size, "Only one monitor should be ACTIVE at this point")

        assertEquals(ThermalMonitorStatus.ACTIVE, it.manager.thermalMonitors.findThermalMonitor(monitorToActivate.id!!).status, "Monitor ${monitorToActivate.id} should be ACTIVE at this point")

        val activeFrom10HoursAgo = OffsetDateTime.now().minusHours(10)
        val activeTo1HourAgo = OffsetDateTime.now().minusHours(1)
        val thermalMonitor3 = ThermalMonitor(
            name = "test",
            status = ThermalMonitorStatus.ACTIVE,
            thermometerIds = arrayOf(),
            lowerThresholdTemperature = -50f,
            upperThresholdTemperature = 50f,
            activeFrom = activeFrom10HoursAgo.toString(),
            monitorType = ThermalMonitorType.ONE_OFF,
            activeTo = activeTo1HourAgo.toString()
        )

        val monitorToFinish = it.manager.thermalMonitors.create(thermalMonitor3)
        assertEquals(2, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.ACTIVE).size, "There should be two ACTIVE monitors")
        assertEquals(0, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.FINISHED).size, "No monitor should be FINISHED yet")

        it.setCronKey().thermalMonitors.resolveMonitorStatuses()

        assertEquals(1, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.FINISHED).size, "There should be only one FINISHED monitor at this point")
        assertEquals(1, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.ACTIVE).size, "There should be only one ACTIVE monitor at this point")

        assertEquals(ThermalMonitorStatus.FINISHED, it.manager.thermalMonitors.findThermalMonitor(monitorToFinish.id!!).status, "Monitor ${monitorToFinish.id} should be FINISHED at this point")
    }

    @Test
    fun testCreateScheduledMonitor() = createTestBuilder().use {
        val thermalMonitor = ThermalMonitor(
            name = "Test",
            status = ThermalMonitorStatus.ACTIVE,
            thermometerIds = arrayOf(),
            monitorType = ThermalMonitorType.SCHEDULED,
            schedule = arrayOf(
                ThermalMonitorSchedulePeriod(
                    start = ThermalMonitorScheduleDate(
                        weekday = ThermalMonitorScheduleWeekDay.MONDAY,
                        hour = 10,
                        minute = 0
                    ),
                    end = ThermalMonitorScheduleDate(
                        weekday = ThermalMonitorScheduleWeekDay.FRIDAY,
                        hour = 20,
                        minute = 6
                    )
                )
            )
        )

        val created = it.manager.thermalMonitors.create(thermalMonitor)
        val found = it.manager.thermalMonitors.findThermalMonitor(created.id!!)
        assertEquals(1, found.schedule!!.size, "Monitor should have one schedule period")
        val schedulePeriod = found.schedule.first()
        assertEquals(ThermalMonitorScheduleWeekDay.MONDAY, schedulePeriod.start.weekday, "Schedule start weekday should be MONDAY")
        assertEquals(10, schedulePeriod.start.hour, "Schedule start hour should be 10")
        assertEquals(0, schedulePeriod.start.minute, "Schedule start minute should be 0")

        assertEquals(ThermalMonitorScheduleWeekDay.FRIDAY, schedulePeriod.end.weekday, "Schedule end weekday should be FRIDAY")
        assertEquals(20, schedulePeriod.end.hour, "Schedule end hour should be 20")
        assertEquals(6, schedulePeriod.end.minute, "Schedule end minute should be 6")

        it.manager.thermalMonitors.assertCreateFail(
            expectedStatus = 400,
            thermalMonitor = thermalMonitor.copy(
                schedule = arrayOf(
                    ThermalMonitorSchedulePeriod(
                        start = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.SUNDAY,
                            hour = 10,
                            minute = 0
                        ),
                        end = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.FRIDAY,
                            hour = 20,
                            minute = 6
                        )
                    )
                )
            )
        )

        it.manager.thermalMonitors.assertCreateFail(
            expectedStatus = 400,
            thermalMonitor = thermalMonitor.copy(
                schedule = arrayOf(
                    ThermalMonitorSchedulePeriod(
                        start = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.FRIDAY,
                            hour = 20,
                            minute = 0
                        ),
                        end = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.FRIDAY,
                            hour = 10,
                            minute = 6
                        )
                    )
                )
            )
        )

        it.manager.thermalMonitors.assertCreateFail(
            expectedStatus = 400,
            thermalMonitor = thermalMonitor.copy(
                schedule = arrayOf(
                    ThermalMonitorSchedulePeriod(
                        start = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.FRIDAY,
                            hour = 10,
                            minute = 10
                        ),
                        end = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.FRIDAY,
                            hour = 10,
                            minute = 6
                        )
                    )
                )
            )
        )

        it.manager.thermalMonitors.assertCreateFail(
            expectedStatus = 400,
            thermalMonitor = thermalMonitor.copy(
                schedule = arrayOf(
                    ThermalMonitorSchedulePeriod(
                        start = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.MONDAY,
                            hour = 10,
                            minute = 1000
                        ),
                        end = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.FRIDAY,
                            hour = 10,
                            minute = 6
                        )
                    )
                )
            )
        )


        it.manager.thermalMonitors.assertCreateFail(
            expectedStatus = 400,
            thermalMonitor = thermalMonitor.copy(
                schedule = arrayOf(
                    ThermalMonitorSchedulePeriod(
                        start = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.MONDAY,
                            hour = 30,
                            minute = 20
                        ),
                        end = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.FRIDAY,
                            hour = 10,
                            minute = 6
                        )
                    )
                )
            )
        )

        it.manager.thermalMonitors.assertCreateFail(
            expectedStatus = 400,
            thermalMonitor = thermalMonitor.copy(
                status = ThermalMonitorStatus.PENDING
            )
        )

        it.manager.thermalMonitors.assertCreateFail(
            expectedStatus = 400,
            thermalMonitor = thermalMonitor.copy(
                status = ThermalMonitorStatus.FINISHED
            )
        )
    }

    @Test
    fun testUpdateScheduledMonitor() = createTestBuilder().use {
        val thermalMonitor = ThermalMonitor(
            name = "Test",
            status = ThermalMonitorStatus.ACTIVE,
            thermometerIds = arrayOf(),
            monitorType = ThermalMonitorType.SCHEDULED,
            schedule = arrayOf(
                ThermalMonitorSchedulePeriod(
                    start = ThermalMonitorScheduleDate(
                        weekday = ThermalMonitorScheduleWeekDay.MONDAY,
                        hour = 10,
                        minute = 0
                    ),
                    end = ThermalMonitorScheduleDate(
                        weekday = ThermalMonitorScheduleWeekDay.FRIDAY,
                        hour = 20,
                        minute = 6
                    )
                )
            )
        )

        val created = it.manager.thermalMonitors.create(thermalMonitor)
        it.manager.thermalMonitors.update(
            id = created.id!!,
            thermalMonitor = created.copy(
                schedule = arrayOf(
                    ThermalMonitorSchedulePeriod(
                        start = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.TUESDAY,
                            hour = 12,
                            minute = 5
                        ),
                        end = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.SUNDAY,
                            hour = 18,
                            minute = 30
                        )
                    )
                )
            )
        )

        val found = it.manager.thermalMonitors.findThermalMonitor(created.id)

        assertEquals(1, found.schedule!!.size, "Monitor should have one schedule period")
        val schedulePeriod = found.schedule.first()
        assertEquals(ThermalMonitorScheduleWeekDay.TUESDAY, schedulePeriod.start.weekday, "Schedule start weekday should be TUESDAY")
        assertEquals(12, schedulePeriod.start.hour, "Schedule start hour should be 12 after the update")
        assertEquals(5, schedulePeriod.start.minute, "Schedule start minute should be 5 after the update")
        assertEquals(ThermalMonitorScheduleWeekDay.SUNDAY, schedulePeriod.end.weekday, "Schedule end weekday should be SUNDAY")
        assertEquals(18, schedulePeriod.end.hour, "Schedule end hour should be 18 after the update")
        assertEquals(30, schedulePeriod.end.minute, "Schedule end minute should be 30 after the update")

        it.manager.thermalMonitors.assertUpdateFail(
            expectedStatus = 400,
            id = created.id,
            thermalMonitor = found.copy(
                schedule = null
            )
        )

        it.manager.thermalMonitors.assertUpdateFail(
            expectedStatus = 400,
            id = created.id,
            thermalMonitor = found.copy(
                schedule = arrayOf(
                    ThermalMonitorSchedulePeriod(
                        start = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.SUNDAY,
                            hour = 10,
                            minute = 0
                        ),
                        end = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.FRIDAY,
                            hour = 20,
                            minute = 6
                        )
                    )
                )
            )
        )

        it.manager.thermalMonitors.assertUpdateFail(
            expectedStatus = 400,
            id = created.id,
            thermalMonitor = found.copy(
                schedule = arrayOf(
                    ThermalMonitorSchedulePeriod(
                        start = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.FRIDAY,
                            hour = 20,
                            minute = 0
                        ),
                        end = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.FRIDAY,
                            hour = 10,
                            minute = 6
                        )
                    )
                )
            )
        )

        it.manager.thermalMonitors.assertUpdateFail(
            expectedStatus = 400,
            id = created.id,
            thermalMonitor = found.copy(
                schedule = arrayOf(
                    ThermalMonitorSchedulePeriod(
                        start = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.FRIDAY,
                            hour = 10,
                            minute = 10
                        ),
                        end = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.FRIDAY,
                            hour = 10,
                            minute = 6
                        )
                    )
                )
            )
        )

        it.manager.thermalMonitors.assertUpdateFail(
            expectedStatus = 400,
            id = created.id,
            thermalMonitor = found.copy(
                schedule = arrayOf(
                    ThermalMonitorSchedulePeriod(
                        start = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.MONDAY,
                            hour = 10,
                            minute = 1000
                        ),
                        end = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.FRIDAY,
                            hour = 10,
                            minute = 6
                        )
                    )
                )
            )
        )


        it.manager.thermalMonitors.assertUpdateFail(
            expectedStatus = 400,
            id = created.id,
            thermalMonitor = found.copy(
                schedule = arrayOf(
                    ThermalMonitorSchedulePeriod(
                        start = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.MONDAY,
                            hour = 30,
                            minute = 20
                        ),
                        end = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.FRIDAY,
                            hour = 10,
                            minute = 6
                        )
                    )
                )
            )
        )

        it.manager.thermalMonitors.assertCreateFail(
            expectedStatus = 400,
            thermalMonitor = found.copy(
                status = ThermalMonitorStatus.PENDING
            )
        )

        it.manager.thermalMonitors.assertCreateFail(
            expectedStatus = 400,
            thermalMonitor = found.copy(
                status = ThermalMonitorStatus.FINISHED
            )
        )
    }

    @Test
    fun testScheduledMonitorStatusResolve() = createTestBuilder().use {
        val time = OffsetDateTime.now()

        val (inactive1Start, inactive1End) = if (time.dayOfWeek.value != 6)  {
            Pair(
                time.plusDays(1),
                time.plusDays(2)
            )
        } else {
            Pair(
                time.plusDays(2),
                time.plusDays(3)
            )
        }

        val thermalMonitor = ThermalMonitor(
            name = "Test",
            status = ThermalMonitorStatus.INACTIVE,
            thermometerIds = arrayOf(),
            monitorType = ThermalMonitorType.SCHEDULED,
            schedule = arrayOf(
                ThermalMonitorSchedulePeriod(
                    start = ThermalMonitorScheduleDate(
                        weekday = ThermalMonitorScheduleWeekDay.entries[inactive1Start.dayOfWeek.value - 1],
                        hour = inactive1Start.hour,
                        minute = inactive1Start.minute
                    ),
                    end = ThermalMonitorScheduleDate(
                        weekday = ThermalMonitorScheduleWeekDay.entries[inactive1End.dayOfWeek.value - 1],
                        hour = inactive1End.hour,
                        minute = inactive1End.minute
                    )
                )
            )
        )

        val created = it.manager.thermalMonitors.create(
            thermalMonitor = thermalMonitor
        )

        it.setCronKey().thermalMonitors.resolveMonitorStatuses()

        assertEquals(1, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.INACTIVE).size, "There should be one INACTIVE monitor")
        assertEquals(0, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.ACTIVE).size, "There should be no ACTIVE monitors")

        val (active1Start, active1End) = when (time.dayOfWeek.value)  {
            7 -> Pair(
                time.minusDays(1),
                time.plusMinutes(10)
            )
            1 -> Pair(
                time,
                time.plusDays(1)
            )
            else -> Pair(
                time.minusDays(1),
                time.plusDays(1)
            )
        }

        it.manager.thermalMonitors.update(
            id = created.id!!,
            thermalMonitor = created.copy(
                schedule = arrayOf(
                    ThermalMonitorSchedulePeriod(
                        start = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.entries[active1Start.dayOfWeek.value - 1],
                            hour = active1Start.hour,
                            minute = active1Start.minute
                        ),
                        end = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.entries[active1End.dayOfWeek.value - 1],
                            hour = active1End.hour,
                            minute = active1End.minute
                        )
                    )
                )
            )
        )

        it.setCronKey().thermalMonitors.resolveMonitorStatuses()

        assertEquals(0, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.INACTIVE).size, "There should be no INACTIVE monitors")
        assertEquals(1, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.ACTIVE).size, "There should be one ACTIVE monitor")

        val inactive2Start = time.plusHours(1)
        val inactive2End = time.plusHours(2)

        it.manager.thermalMonitors.update(
            id = created.id,
            thermalMonitor = created.copy(
                status = ThermalMonitorStatus.ACTIVE,
                schedule = arrayOf(
                    ThermalMonitorSchedulePeriod(
                        start = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.entries[inactive2Start.dayOfWeek.value - 1],
                            hour = inactive2Start.hour,
                            minute = inactive2Start.minute
                        ),
                        end = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.entries[inactive2End.dayOfWeek.value - 1],
                            hour = inactive2End.hour,
                            minute = inactive2End.minute
                        )
                    )
                )
            )
        )

        it.setCronKey().thermalMonitors.resolveMonitorStatuses()

        assertEquals(1, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.INACTIVE).size, "There should be one INACTIVE monitor")
        assertEquals(0, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.ACTIVE).size, "There should be no ACTIVE monitors")

        val active2Start = time.minusHours(1)
        val active2End = time.plusHours(1)

        it.manager.thermalMonitors.update(
            id = created.id,
            thermalMonitor = created.copy(
                schedule = arrayOf(
                    ThermalMonitorSchedulePeriod(
                        start = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.entries[active2Start.dayOfWeek.value - 1],
                            hour = active2Start.hour,
                            minute = active2Start.minute
                        ),
                        end = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.entries[active2End.dayOfWeek.value - 1],
                            hour = active2End.hour,
                            minute = active2End.minute
                        )
                    )
                )
            )
        )

        it.setCronKey().thermalMonitors.resolveMonitorStatuses()

        assertEquals(0, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.INACTIVE).size, "There should be no INACTIVE monitors")
        assertEquals(1, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.ACTIVE).size, "There should be one ACTIVE monitor")

        val inactive3Start = time.plusMinutes(10)
        val inactive3End = time.plusMinutes(20)

        it.manager.thermalMonitors.update(
            id = created.id,
            thermalMonitor = created.copy(
                status = ThermalMonitorStatus.ACTIVE,
                schedule = arrayOf(
                    ThermalMonitorSchedulePeriod(
                        start = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.entries[inactive3Start.dayOfWeek.value - 1],
                            hour = inactive3Start.hour,
                            minute = inactive3Start.minute
                        ),
                        end = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.entries[inactive3End.dayOfWeek.value - 1],
                            hour = inactive3End.hour,
                            minute = inactive3End.minute
                        )
                    )
                )
            )
        )

        it.setCronKey().thermalMonitors.resolveMonitorStatuses()

        assertEquals(1, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.INACTIVE).size, "There should be one INACTIVE monitor")
        assertEquals(0, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.ACTIVE).size, "There should be no ACTIVE monitors")

        val active3Start = time.minusMinutes(10)
        val active3End = time.plusMinutes(10)

        it.manager.thermalMonitors.update(
            id = created.id,
            thermalMonitor = created.copy(
                schedule = arrayOf(
                    ThermalMonitorSchedulePeriod(
                        start = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.entries[active3Start.dayOfWeek.value - 1],
                            hour = active3Start.hour,
                            minute = active3Start.minute
                        ),
                        end = ThermalMonitorScheduleDate(
                            weekday = ThermalMonitorScheduleWeekDay.entries[active3End.dayOfWeek.value - 1],
                            hour = active3End.hour,
                            minute = active3End.minute
                        )
                    )
                )
            )
        )

        it.setCronKey().thermalMonitors.resolveMonitorStatuses()

        assertEquals(0, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.INACTIVE).size, "There should be no INACTIVE monitors")
        assertEquals(1, it.manager.thermalMonitors.listThermalMonitors(status = ThermalMonitorStatus.ACTIVE).size, "There should be one ACTIVE monitor")

    }
}