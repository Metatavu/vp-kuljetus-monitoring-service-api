package fi.metatavu.vp.monitoring.monitors

import fi.metatavu.vp.api.model.*
import fi.metatavu.vp.monitoring.monitors.schedules.ThermalMonitorSchedulePeriodController
import fi.metatavu.vp.monitoring.monitors.thermometers.MonitorThermometerController
import fi.metatavu.vp.usermanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class ThermalMonitorTranslator: AbstractTranslator<ThermalMonitorEntity, ThermalMonitor>() {
    @Inject
    lateinit var monitorThermometerController: MonitorThermometerController

    @Inject
    lateinit var thermalMonitorSchedulePeriodController: ThermalMonitorSchedulePeriodController

    /**
     * Translate thermal monitor
     *
     * @param entity thermal monitor
     */
    override suspend fun translate(entity: ThermalMonitorEntity): ThermalMonitor {

        return ThermalMonitor(
            name = entity.name,
            status = ThermalMonitorStatus.valueOf(entity.status),
            thermometerIds = monitorThermometerController.listThermometers(
                thermalMonitorEntity = entity,
                thermometerId = null,
                onlyActive = false
            ).map { it.thermometerId },
            id = entity.id,
            lowerThresholdTemperature = entity.thresholdLow,
            upperThresholdTemperature = entity.thresholdHigh,
            activeTo = entity.activeTo,
            activeFrom = entity.activeFrom,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            monitorType = ThermalMonitorType.valueOf(entity.monitorType),
            schedule = buildSchedule(entity)
        )
    }

    /**
     * Builds a schedule for a thermal monitor from schedule periods found in the database.
     * If no periods are found, return null.
     *
     * @param entity thermal monitor
     */
    private suspend fun buildSchedule(entity: ThermalMonitorEntity): List<ThermalMonitorSchedulePeriod>? {
        val periods =  thermalMonitorSchedulePeriodController.list(thermalMonitor = entity)
        if (periods.isEmpty()) {
            return null
        }

        return periods.map { schedule ->
            ThermalMonitorSchedulePeriod(
                start = ThermalMonitorScheduleDate(
                    weekday = ThermalMonitorScheduleWeekDay.entries[schedule.startWeekDay!!],
                    hour = schedule.startHour!!,
                    minute = schedule.startMinute!!
                ),
                end = ThermalMonitorScheduleDate(
                    weekday = ThermalMonitorScheduleWeekDay.entries[schedule.endWeekDay!!],
                    hour = schedule.endHour!!,
                    minute = schedule.endMinute!!
                )
            )
        }
    }
}