package fi.metatavu.vp.monitoring.incidents

import fi.metatavu.vp.api.model.ThermalMonitorIncident
import fi.metatavu.vp.api.model.ThermalMonitorIncidentPagedPolicy
import fi.metatavu.vp.api.model.ThermalMonitorIncidentStatus
import fi.metatavu.vp.monitoring.incidents.pagedpolicies.PagedPolicyRepository
import fi.metatavu.vp.usermanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class ThermalMonitorIncidentTranslator: AbstractTranslator<ThermalMonitorIncidentEntity, ThermalMonitorIncident>() {
    @Inject
    lateinit var pagedPolicyRepository: PagedPolicyRepository

    /**
     * Translate thermal monitor incident database entity into REST entity
     *
     * @param entity
     */
    override suspend fun translate(entity: ThermalMonitorIncidentEntity): ThermalMonitorIncident {
        return ThermalMonitorIncident(
            id = entity.id,
            monitorId = entity.thermalMonitor.id,
            thermometerId = entity.monitorThermometer.thermometerId,
            temperature = entity.temperature,
            status = ThermalMonitorIncidentStatus.valueOf(entity.status),
            timestamp = entity.triggeredAt,
            resolvedAt = entity.resolvedAt,
            acknowledgeAt = entity.acknowledgedAt,
            resolvedBy = entity.resolvedBy,
            acknowledgedBy = entity.acknowledgedBy,
            pagedPolicies = pagedPolicyRepository.listByIncident(entity).map {
                ThermalMonitorIncidentPagedPolicy(
                    id = it.id,
                    policyId = it.policy.id,
                    incidentId = entity.id,
                    contactId = it.policy.pagingPolicyContact.id,
                    time = it.createdAt
                )
            }
        )
    }
}