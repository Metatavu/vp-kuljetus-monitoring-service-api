package fi.metatavu.vp.monitoring.monitors

import fi.metatavu.vp.api.model.ThermalMonitor
import fi.metatavu.vp.api.model.ThermalMonitorStatus
import fi.metatavu.vp.api.model.ThermalMonitorType
import fi.metatavu.vp.api.spec.ThermalMonitorsApi
import fi.metatavu.vp.monitoring.monitors.schedules.ThermalMonitorSchedulePeriodController
import fi.metatavu.vp.monitoring.rest.AbstractApi
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.time.OffsetDateTime
import java.util.*

@RequestScoped
@WithSession
class ThermalMonitorsApiImpl: ThermalMonitorsApi, AbstractApi() {
    @Inject
    lateinit var thermalMonitorController: ThermalMonitorController

    @Inject
    lateinit var thermalMonitorTranslator: ThermalMonitorTranslator

    @Inject
    lateinit var thermalMonitorSchedulePeriodController: ThermalMonitorSchedulePeriodController

    @ConfigProperty(name = "vp.monitoring.cron.apiKey")
    lateinit var cronKey: String

    @ConfigProperty(name = "vp.env")
    var env: String? = null

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun createThermalMonitor(thermalMonitor: ThermalMonitor): Uni<Response> = withCoroutineScope {
        loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)

        if (thermalMonitor.monitorType == ThermalMonitorType.SINGULAR) {
            if (thermalMonitor.schedule != null) {
                return@withCoroutineScope createBadRequest("Monitors with monitorType SINGULAR are not allowed to have a schedule")
            }

            if (thermalMonitor.status == ThermalMonitorStatus.INACTIVE) {
                return@withCoroutineScope createBadRequest("Status INACTIVE is not allowed for singular monitors")
            }
        }

        if (thermalMonitor.monitorType == ThermalMonitorType.SCHEDULED) {
            if (thermalMonitor.schedule?.isNotEmpty() != true) {
                return@withCoroutineScope createBadRequest("Monitors with monitorType SCHEDULED must have a schedule")
            }

            if (thermalMonitor.status == ThermalMonitorStatus.PENDING || thermalMonitor.status == ThermalMonitorStatus.FINISHED) {
                return@withCoroutineScope createBadRequest("Status ${thermalMonitor.status} is not allowed for scheduled monitors")
            }
        }

        if (thermalMonitor.schedule != null) {
            thermalMonitor.schedule.forEach { schedulePeriod ->
                val startHour = schedulePeriod.start.hour
                val startMinute = schedulePeriod.start.minute
                if (!thermalMonitorSchedulePeriodController.isScheduleTimeValid(startHour, startMinute)) {
                    return@withCoroutineScope createBadRequest("Invalid start time $startHour:$startMinute for a schedule period")
                }

                val endHour = schedulePeriod.end.hour
                val endMinute = schedulePeriod.end.minute
                if (!thermalMonitorSchedulePeriodController.isScheduleTimeValid(endHour, endMinute)) {
                    return@withCoroutineScope createBadRequest("Invalid end time $endHour:$endMinute for a schedule period")
                }

                if (!thermalMonitorSchedulePeriodController.isSchedulePeriodStartBeforeEnd(schedulePeriod)) {
                    return@withCoroutineScope createBadRequest("Schedule start time must be before end time")
                }
            }
        }

        createOk(thermalMonitorTranslator.translate(thermalMonitorController.create(
            thermalMonitor = thermalMonitor,
            creatorId = loggedUserId!!
        )))
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun deleteThermalMonitor(thermalMonitorId: UUID): Uni<Response> = withCoroutineScope {
        if (env != "TEST") {
            return@withCoroutineScope createForbidden("Deleting monitors is not allowed")
        }

        val found = thermalMonitorController.find(thermalMonitorId) ?: return@withCoroutineScope createNotFound()
        thermalMonitorController.delete(found)

        createNoContent()
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun findThermalMonitor(thermalMonitorId: UUID): Uni<Response> = withCoroutineScope {
        val found = thermalMonitorController.find(thermalMonitorId) ?: return@withCoroutineScope createNotFound()
        createOk(thermalMonitorTranslator.translate(found))
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun listThermalMonitors(
        status: ThermalMonitorStatus?,
        activeAfter: OffsetDateTime?,
        activeBefore: OffsetDateTime?,
        first: Int?,
        max: Int?
    ): Uni<Response> = withCoroutineScope {
        val list = thermalMonitorController.list(
            status = status,
            activeBefore = activeBefore,
            activeAfter = activeAfter,
            first = first,
            max =  max
        )

        createOk(list.map{ thermalMonitorEntity -> thermalMonitorTranslator.translate(thermalMonitorEntity) })
    }

    @WithSession
    @WithTransaction
    override fun resolveMonitorStatuses(): Uni<Response> = withCoroutineScope {
        if (requestCronKey != cronKey) {
            return@withCoroutineScope createUnauthorized(UNAUTHORIZED)
        }

        thermalMonitorController.resolveSingularMonitorStatuses()

        thermalMonitorController.resolveScheduledMonitorStatuses()

        createOk()
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun updateThermalMonitor(thermalMonitorId: UUID, thermalMonitor: ThermalMonitor): Uni<Response> = withCoroutineScope {
        loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)

        if (thermalMonitor.monitorType == ThermalMonitorType.SINGULAR) {
            if (thermalMonitor.schedule != null) {
                return@withCoroutineScope createBadRequest("Monitors with monitorType SINGULAR are not allowed to have a schedule")
            }

            if (thermalMonitor.status == ThermalMonitorStatus.INACTIVE) {
                return@withCoroutineScope createBadRequest("Status INACTIVE is not allowed for singular monitors")
            }
        }

        if (thermalMonitor.monitorType == ThermalMonitorType.SCHEDULED) {
            if (thermalMonitor.schedule?.isNotEmpty() != true) {
                return@withCoroutineScope createBadRequest("Monitors with monitorType SCHEDULED must have a schedule")
            }

            if (thermalMonitor.status == ThermalMonitorStatus.PENDING || thermalMonitor.status == ThermalMonitorStatus.FINISHED) {
                return@withCoroutineScope createBadRequest("Status ${thermalMonitor.status} is not allowed for scheduled monitors")
            }
        }

        if (thermalMonitor.schedule != null) {
            thermalMonitor.schedule.forEach { schedulePeriod ->
                val startHour = schedulePeriod.start.hour
                val startMinute = schedulePeriod.start.minute
                if (!thermalMonitorSchedulePeriodController.isScheduleTimeValid(startHour, startMinute)) {
                    return@withCoroutineScope createBadRequest("Invalid start time $startHour:$startMinute for a schedule period")
                }

                val endHour = schedulePeriod.end.hour
                val endMinute = schedulePeriod.end.minute
                if (!thermalMonitorSchedulePeriodController.isScheduleTimeValid(endHour, endMinute)) {
                    return@withCoroutineScope createBadRequest("Invalid end time $endHour:$endMinute for a schedule period")
                }

                if (!thermalMonitorSchedulePeriodController.isSchedulePeriodStartBeforeEnd(schedulePeriod)) {
                    return@withCoroutineScope createBadRequest("Schedule start time must be before end time")
                }
            }
        }

        val found = thermalMonitorController.find(thermalMonitorId) ?: return@withCoroutineScope createNotFound()

        val updated = if (env == "TEST") {
            thermalMonitorController.updateFromRest(
                thermalMonitor = thermalMonitor,
                thermalMonitorEntity = found,
                modifier = loggedUserId!!,
                deleteUnusedThermometersPermanently = true)
        } else {
            thermalMonitorController.updateFromRest(
                thermalMonitor = thermalMonitor,
                thermalMonitorEntity = found,
                modifier = loggedUserId!!,
                deleteUnusedThermometersPermanently = false)
        }
        createOk(thermalMonitorTranslator.translate(updated))
    }
}