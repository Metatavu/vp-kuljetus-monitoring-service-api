package fi.metatavu.vp.monitoring.monitors.schedules

import fi.metatavu.vp.monitoring.monitors.ThermalMonitorEntity
import fi.metatavu.vp.monitoring.persistence.AbstractRepository
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
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
     */
    suspend fun list(thermalMonitor: ThermalMonitorEntity?): List<ThermalMonitorSchedulePeriodEntity> {
        val queryBuilder = StringBuilder()
        val parameters = Parameters()

        if (thermalMonitor != null) {
            addCondition(queryBuilder, "thermalMonitor = :thermalMonitor")
            parameters.and("thermalMonitor", thermalMonitor)
        }

        return list(queryBuilder.toString(), parameters).awaitSuspending()
    }
}