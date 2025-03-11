package fi.metatavu.vp.monitoring.policies

import fi.metatavu.vp.api.model.PagingPolicyType
import fi.metatavu.vp.monitoring.monitors.ThermalMonitorEntity
import fi.metatavu.vp.monitoring.policies.contacts.PagingPolicyContactEntity
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

@ApplicationScoped
class PagingPolicyController {

    @Inject
    lateinit var pagingPolicyRepository: PagingPolicyRepository

    /**
     * Save a thermal monitor paging policy to the database
     *
     * @param policyType
     * @param priority
     * @param escalationSeconds
     * @param pagingPolicyContact
     * @param thermalMonitor
     * @param creatorId
     */
    suspend fun create(
        policyType: PagingPolicyType,
        priority: Int,
        escalationSeconds: Int,
        pagingPolicyContact: PagingPolicyContactEntity,
        thermalMonitor: ThermalMonitorEntity,
        creatorId: UUID
    ): ThermalMonitorPagingPolicyEntity {
        return pagingPolicyRepository.create(
            policyType = policyType.toString(),
            priority = priority,
            escalationSeconds = escalationSeconds,
            pagingPolicyContact = pagingPolicyContact,
            thermalMonitor = thermalMonitor,
            creatorId = creatorId
        )
    }

    /**
     * Retrieve policy by id if exists
     *
     * @param id
     */
    suspend fun find(id: UUID): ThermalMonitorPagingPolicyEntity? {
        return pagingPolicyRepository.findByIdSuspending(id)
    }

    /**
     * Delete policy from the database
     *
     * @param thermalMonitorPagingPolicyEntity
     */
    suspend fun delete(thermalMonitorPagingPolicyEntity: ThermalMonitorPagingPolicyEntity) {
        pagingPolicyRepository.deleteSuspending(thermalMonitorPagingPolicyEntity)
    }

    /**
     * Delete all policies from the database that belong to a given contact
     *
     * @param pagingPolicyContact
     */
    suspend fun deletePoliciesByContact(pagingPolicyContact: PagingPolicyContactEntity) {
        pagingPolicyRepository.listAllByContact(pagingPolicyContact).forEach { delete(it) }
    }

    /**
     * Delete all policies from the database that belong to a given monitor
     *
     * @param thermalMonitor
     */
    suspend fun deletePoliciesByMonitor(thermalMonitor: ThermalMonitorEntity) {
        pagingPolicyRepository.listAllByMonitor(thermalMonitor).forEach { delete(it) }
    }

    /**
     * List policies in the database
     *
     * @param thermalMonitor
     * @param first
     * @param max
     */
    suspend fun list(thermalMonitor: ThermalMonitorEntity, first: Int, max: Int): List<ThermalMonitorPagingPolicyEntity> {
        return pagingPolicyRepository.list(thermalMonitor = thermalMonitor, first = first, max = max).first
    }

    /**
     * Update policy data in the database
     *
     * @param entityToUpdate
     * @param policyType
     * @param priority
     * @param escalationDelaySeconds
     * @param pagingPolicyContact
     * @param modifierId
     */
    suspend fun update(
        entityToUpdate: ThermalMonitorPagingPolicyEntity,
        policyType: PagingPolicyType,
        priority: Int,
        escalationDelaySeconds: Int,
        pagingPolicyContact: PagingPolicyContactEntity,
        modifierId: UUID
    ): ThermalMonitorPagingPolicyEntity {
        return pagingPolicyRepository.update(
            entityToUpdate = entityToUpdate,
            policyType = policyType.toString(),
            priority = priority,
            escalationDelaySeconds = escalationDelaySeconds,
            pagingPolicyContact = pagingPolicyContact,
            modifierId = modifierId
        )
    }
}