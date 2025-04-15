package fi.metatavu.vp.monitoring.monitors.schedules

import fi.metatavu.vp.api.model.ThermalMonitorScheduleDate
import fi.metatavu.vp.api.model.ThermalMonitorSchedulePeriod
import fi.metatavu.vp.api.model.ThermalMonitorScheduleWeekDay
import fi.metatavu.vp.api.model.ThermalMonitorStatus
import fi.metatavu.vp.monitoring.monitors.ThermalMonitorEntity
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.OffsetDateTime

@ApplicationScoped
class ThermalMonitorSchedulePeriodController {
    @Inject
    lateinit var thermalMonitorSchedulePeriodRepository: ThermalMonitorSchedulePeriodRepository

    /**
     * Save a thermal monitor schedule date to the database
     *
     * @param thermalMonitor
     * @param startWeekDay
     * @param startHour
     * @param startMinute
     * @param endWeekDay
     * @param endHour
     * @param endMinute
     */
    suspend fun create(
        thermalMonitor: ThermalMonitorEntity,
        startWeekDay: ThermalMonitorScheduleWeekDay,
        startHour: Int,
        startMinute: Int,
        endWeekDay: ThermalMonitorScheduleWeekDay,
        endHour: Int,
        endMinute: Int
    ): ThermalMonitorSchedulePeriodEntity {
        return thermalMonitorSchedulePeriodRepository.create(
            thermalMonitor = thermalMonitor,
            startWeekDay = translateWeekDayToNumber(startWeekDay),
            startHour = startHour,
            startMinute = startMinute,
            endWeekDay = translateWeekDayToNumber(endWeekDay),
            endHour = endHour,
            endMinute = endMinute
        )
    }

    /**
     * List thermal monitor schedule periods with given filters from the database
     *
     * @param thermalMonitor
     * @param activeAt
     * @param thermalMonitorStatus
     *
     */
    suspend fun list(
        thermalMonitor: ThermalMonitorEntity? = null,
        activeAt: OffsetDateTime? = null,
        thermalMonitorStatus: ThermalMonitorStatus? = null
        ): List<ThermalMonitorSchedulePeriodEntity> {
        return thermalMonitorSchedulePeriodRepository.list(
            thermalMonitor = thermalMonitor,
            activeAt = activeAt,
            thermalMonitorStatus = thermalMonitorStatus?.toString()
        )
    }

    /**
     * Delete a thermal monitor schedule period from the database
     *
     * @param thermalMonitorSchedulePeriodEntity
     */
    suspend fun delete(thermalMonitorSchedulePeriodEntity: ThermalMonitorSchedulePeriodEntity) {
        thermalMonitorSchedulePeriodRepository.deleteSuspending(thermalMonitorSchedulePeriodEntity)
    }

    /**
     * Validate that hours and minutes are valid
     *
     * @param hour
     * @param minute
     */
    fun isScheduleTimeValid(hour: Int, minute: Int): Boolean {
        return hour in 0..23 && minute in 0..59
    }

    /**
     * Validate that schedule period start time is before end time
     *
     * @param period
     */
    fun isSchedulePeriodStartBeforeEnd(period: ThermalMonitorSchedulePeriod): Boolean {
        return scheduleDateToMinutes(period.start) < scheduleDateToMinutes(period.end)
    }

    /**
     * Translates a schedule date to minutes.
     *
     * @param scheduleDate
     */
    private fun scheduleDateToMinutes(scheduleDate: ThermalMonitorScheduleDate): Int {
        return (scheduleDate.weekday.ordinal + 1) * 24 * 60 +
            scheduleDate.hour * 60 +
            scheduleDate.minute
    }

    /**
     * Translates a weekday to a number.
     * Monday will be 0, tuesday 1, etc.
     *
     * @param weekDay
     */
    private fun translateWeekDayToNumber(weekDay: ThermalMonitorScheduleWeekDay): Int {
        return weekDay.ordinal
    }
}