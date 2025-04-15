package fi.metatavu.vp.monitoring.monitors.schedules

import fi.metatavu.vp.monitoring.monitors.ThermalMonitorEntity
import fi.metatavu.vp.monitoring.persistence.AbstractRepository
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.time.OffsetDateTime
import java.util.UUID

@ApplicationScoped
class ThermalMonitorSchedulePeriodRepository: AbstractRepository<ThermalMonitorSchedulePeriodEntity, UUID>() {
    /**
     * Save a thermal monitor schedule period to the database
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
        startWeekDay: Int,
        startHour: Int,
        startMinute: Int,
        endWeekDay: Int,
        endHour: Int,
        endMinute: Int
    ): ThermalMonitorSchedulePeriodEntity {
        val scheduleDate = ThermalMonitorSchedulePeriodEntity()
        scheduleDate.id = UUID.randomUUID()
        scheduleDate.startWeekDay = startWeekDay
        scheduleDate.startHour = startHour
        scheduleDate.startMinute = startMinute
        scheduleDate.endWeekDay = endWeekDay
        scheduleDate.endMinute = endMinute
        scheduleDate.endHour = endHour
        scheduleDate.thermalMonitor = thermalMonitor

        return persistSuspending(scheduleDate)
    }

    /**
     * List thermal monitor schedule periods with given filters from the database
     *
     * @param thermalMonitor
     * @param activeAt
     * @param thermalMonitorStatus
     */
    suspend fun list(
        thermalMonitor: ThermalMonitorEntity?,
        activeAt: OffsetDateTime?,
        thermalMonitorStatus: String?
    ): List<ThermalMonitorSchedulePeriodEntity> {
        val queryBuilder = StringBuilder()
        val parameters = Parameters()

        if (thermalMonitor != null) {
            addCondition(queryBuilder, "thermalMonitor = :thermalMonitor")
            parameters.and("thermalMonitor", thermalMonitor)
        }

        if (activeAt != null) {
            addCondition(queryBuilder, "startWeekDay <= :activeAtWeekDay OR (startWeekDay = :activeAtWeekDay AND startHour <= :activeAtHour) OR (startWeekDay = :activeAtWeekDay AND startHour = :activeAtHour AND startMinute <= :activeAtMinute)")
            addCondition(queryBuilder, "endWeekDay >= :activeAtWeekDay OR (endWeekDay = :activeAtWeekDay AND endHour >= :activeAtHour) OR (endWeekDay = :activeAtWeekDay AND endHour = :activeAtHour AND endMinute >= :activeAtMinute)")
            parameters.and("activeAtWeekDay", activeAt.dayOfWeek.value - 1).and("activeAtHour", activeAt.hour).and("activeAtMinute", activeAt.minute)
        }

        if (thermalMonitorStatus != null) {
            addCondition(queryBuilder, "thermalMonitor.status = :status")
            parameters.and("status", thermalMonitorStatus)
        }

        return list(queryBuilder.toString(), parameters).awaitSuspending()
    }


}