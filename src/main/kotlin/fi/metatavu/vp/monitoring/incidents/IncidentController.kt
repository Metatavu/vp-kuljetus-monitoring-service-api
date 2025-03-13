package fi.metatavu.vp.monitoring.incidents

import fi.metatavu.vp.api.model.ThermalMonitorIncidentStatus
import fi.metatavu.vp.monitoring.monitors.ThermalMonitorEntity
import fi.metatavu.vp.monitoring.monitors.thermometers.MonitorThermometerEntity
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.OffsetDateTime
import java.util.*

@ApplicationScoped
class IncidentController {
    @Inject
    lateinit var incidentRepository: IncidentRepository

    /**
     * This is used by the event controller to create incidents
     *
     * @param monitorThermometer
     * @param thermalMonitor
     * @param temperature
     */
    suspend fun create(
        monitorThermometer: MonitorThermometerEntity,
        thermalMonitor: ThermalMonitorEntity,
        temperature: Float?,
    ): ThermalMonitorIncidentEntity {
        return incidentRepository.create(
            status = ThermalMonitorIncidentStatus.TRIGGERED.toString(),
            triggeredAt = OffsetDateTime.now(),
            monitorThermometer = monitorThermometer,
            thermalMonitor = thermalMonitor,
            temperature = temperature,
        )
    }

    /**
     * Retrieve an incident from the database by id if exists
     *
     * @param id
     */
    suspend fun find(id: UUID): ThermalMonitorIncidentEntity? {
        return incidentRepository.findByIdSuspending(id)
    }

    /**
     * Remove an incident from the database
     *
     * @param incident
     */
    suspend fun delete(incident: ThermalMonitorIncidentEntity) {
        incidentRepository.deleteSuspending(incident)
    }

    /**
     * Resolves or acknowledges an incident if that has not been done yet
     *
     * @param incident
     * @param newStatus
     * @param modifier
     */
    suspend fun updateIncidentStatus(incident: ThermalMonitorIncidentEntity, newStatus: ThermalMonitorIncidentStatus, modifier: UUID): ThermalMonitorIncidentEntity {
        return if (newStatus == ThermalMonitorIncidentStatus.ACKNOWLEDGED && incident.status != ThermalMonitorIncidentStatus.ACKNOWLEDGED.toString()) {
            acknowledgeIncident(incident, modifier)
        } else if (newStatus == ThermalMonitorIncidentStatus.RESOLVED && incident.status != ThermalMonitorIncidentStatus.RESOLVED.toString()) {
            resolveIncident(incident, modifier)
        } else {
            incident
        }
    }

    /**
     * Sets incident status to acknowledged
     * This is done manually in the ui
     *
     * @param incident
     * @param acknowledgedBy
     */
    suspend fun acknowledgeIncident(incident: ThermalMonitorIncidentEntity, acknowledgedBy: UUID): ThermalMonitorIncidentEntity {
        return incidentRepository.acknowledgeIncident(
            incident = incident,
            acknowledgedAt =  OffsetDateTime.now(),
            acknowledgedBy =  acknowledgedBy
        )
    }

    /**
     * Sets incident status to resolved
     * This is done manually in the ui or automatically when the root cause of the incident disappears
     *
     * @param incident
     * @param resolvedBy
     */
    suspend fun resolveIncident(incident: ThermalMonitorIncidentEntity, resolvedBy: UUID): ThermalMonitorIncidentEntity {
        return incidentRepository.resolveIncident(
            incident = incident,
            resolvedAt =  OffsetDateTime.now(),
            resolvedBy =  resolvedBy
        )
    }

    /**
     * List incidents from the database
     *
     * @param thermalMonitor
     * @param monitorThermometer
     * @param incidentStatus
     * @param triggeredAfter
     * @param triggeredBefore
     * @param first
     * @param max
     */
    suspend fun list(
        thermalMonitor: ThermalMonitorEntity? = null,
        monitorThermometer: MonitorThermometerEntity? = null,
        thermometerId: UUID? = null,
        incidentStatus: ThermalMonitorIncidentStatus? = null,
        triggeredAfter: OffsetDateTime? = null,
        triggeredBefore: OffsetDateTime? = null,
        first: Int? = null,
        max: Int? = null
    ): List<ThermalMonitorIncidentEntity> {
        return incidentRepository.list(
            thermalMonitor = thermalMonitor,
            monitorThermometer = monitorThermometer,
            thermometerId = thermometerId,
            incidentStatus = incidentStatus.toString(),
            triggeredAfter = triggeredAfter,
            triggeredBefore = triggeredBefore,
            first = first,
            max = max
        ).first
    }
}