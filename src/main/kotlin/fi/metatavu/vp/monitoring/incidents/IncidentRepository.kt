package fi.metatavu.vp.monitoring.incidents

import fi.metatavu.vp.api.model.ThermalMonitorIncidentStatus
import fi.metatavu.vp.monitoring.monitors.ThermalMonitorEntity
import fi.metatavu.vp.monitoring.monitors.thermometers.MonitorThermometerEntity
import fi.metatavu.vp.monitoring.persistence.AbstractRepository
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import java.time.OffsetDateTime
import java.util.UUID

@ApplicationScoped
class IncidentRepository: AbstractRepository<ThermalMonitorIncidentEntity, UUID>() {

    /**
     * Save an incident to the database
     *
     * @param status
     * @param triggeredAt
     * @param monitorThermometer
     * @param thermalMonitor
     * @param temperature
     */
    suspend fun create(
        status: String,
        triggeredAt: OffsetDateTime,
        monitorThermometer: MonitorThermometerEntity,
        thermalMonitor: ThermalMonitorEntity,
        temperature: Float?,
    ): ThermalMonitorIncidentEntity {
        val incident = ThermalMonitorIncidentEntity()
        incident.id = UUID.randomUUID()
        incident.status = status
        incident.triggeredAt = triggeredAt
        incident.acknowledgedAt = null
        incident.resolvedAt = null
        incident.acknowledgedBy = null
        incident.resolvedBy = null
        incident.monitorThermometer = monitorThermometer
        incident.thermalMonitor = thermalMonitor
        incident.temperature = temperature

        return persistSuspending(incident)
    }

    /**
     * Sets incident status to acknowledged
     * This is done manually in the ui
     *
     * @param incident
     * @param acknowledgedAt
     * @param acknowledgedBy
     */
    suspend fun acknowledgeIncident(incident: ThermalMonitorIncidentEntity, acknowledgedAt: OffsetDateTime, acknowledgedBy: UUID): ThermalMonitorIncidentEntity {
        incident.acknowledgedAt = acknowledgedAt
        incident.acknowledgedBy = acknowledgedBy

        incident.status = ThermalMonitorIncidentStatus.ACKNOWLEDGED.toString()
        return persistSuspending(incident)
    }

    /**
     * Sets incident status to resolved
     * This is done manually in the ui or automatically when the root cause of the incident disappears
     *
     * @param incident
     * @param resolvedAt
     * @param resolvedBy
     */
    suspend fun resolveIncident(incident: ThermalMonitorIncidentEntity, resolvedAt: OffsetDateTime, resolvedBy: UUID): ThermalMonitorIncidentEntity {
        incident.resolvedAt = resolvedAt
        incident.resolvedBy = resolvedBy

        incident.status = ThermalMonitorIncidentStatus.RESOLVED.toString()
        return persistSuspending(incident)
    }

    /**
     * List incidents from the database
     *
     * @param thermalMonitor
     * @param monitorThermometer
     * @param thermometerId
     * @param incidentStatus
     * @param triggeredAfter
     * @param triggeredBefore
     * @param first
     * @param max
     */
    suspend fun list(
        thermalMonitor: ThermalMonitorEntity?,
        monitorThermometer: MonitorThermometerEntity?,
        thermometerId: UUID?,
        incidentStatus: String?,
        triggeredAfter: OffsetDateTime?,
        triggeredBefore: OffsetDateTime?,
        first: Int?,
        max: Int?
    ): Pair<List<ThermalMonitorIncidentEntity>, Long> {
        val queryBuilder = StringBuilder()
        val parameters = Parameters()

        if (thermalMonitor != null) {
            addCondition(queryBuilder, "thermalMonitor = :thermalMonitor")
            parameters.and("thermalMonitor", thermalMonitor)
        }

        if (monitorThermometer != null) {
            addCondition(queryBuilder, "monitorThermometer = :monitorThermometer")
            parameters.and("monitorThermometer", monitorThermometer)
        }

        if (thermometerId != null) {
            addCondition(queryBuilder, "monitorThermometer.thermometerId = :thermometerId")
            parameters.and("thermometerId", thermometerId)
        }

        if (incidentStatus != null) {
            addCondition(queryBuilder, "status = :status")
            parameters.and("status", incidentStatus)
        }

        if (triggeredAfter != null) {
            addCondition(queryBuilder, "triggeredAt > :triggeredAfter")
            parameters.and("triggeredAfter", triggeredAfter)
        }

        if (triggeredBefore != null) {
            addCondition(queryBuilder, "triggeredAt < :triggeredBefore")
            parameters.and("triggeredBefore", triggeredBefore)
        }

        return applyFirstMaxToQuery(
            query = find(queryBuilder.toString(), Sort.descending("triggeredAt"), parameters),
            firstIndex = first,
            maxResults = max
        )
    }

}