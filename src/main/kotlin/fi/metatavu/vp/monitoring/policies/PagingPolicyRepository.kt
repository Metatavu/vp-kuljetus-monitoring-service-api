package fi.metatavu.vp.monitoring.policies

import fi.metatavu.vp.monitoring.monitors.ThermalMonitorEntity
import fi.metatavu.vp.monitoring.persistence.AbstractRepository
import fi.metatavu.vp.monitoring.policies.contacts.PagingPolicyContactEntity
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class PagingPolicyRepository: AbstractRepository<ThermalMonitorPagingPolicyEntity, UUID>() {

    /**
     * Save a thermal monitor paging policy to the database
     *
     * @param priority
     * @param escalationSeconds
     * @param pagingPolicyContact
     * @param thermalMonitor
     * @param creatorId
     */
    suspend fun create(
        priority: Int,
        escalationSeconds: Int,
        pagingPolicyContact: PagingPolicyContactEntity,
        thermalMonitor: ThermalMonitorEntity,
        creatorId: UUID
    ): ThermalMonitorPagingPolicyEntity {
        val policy = ThermalMonitorPagingPolicyEntity()

        policy.id = UUID.randomUUID()
        policy.priority = priority
        policy.escalationDelaySeconds = escalationSeconds
        policy.thermalMonitor = thermalMonitor
        policy.pagingPolicyContact = pagingPolicyContact
        policy.creatorId = creatorId
        policy.lastModifierId = creatorId

        return persistSuspending(policy)
    }

    /**
     * List policies in the database
     *
     * @param thermalMonitor
     * @param first
     * @param max
     */
    suspend fun list(
        thermalMonitor: ThermalMonitorEntity,
        first: Int? = null,
        max: Int? = null
    ): Pair<List<ThermalMonitorPagingPolicyEntity>, Long> {
        val queryBuilder = StringBuilder()
        val parameters = Parameters()

        addCondition(queryBuilder, "thermalMonitor = :thermalMonitor")
        parameters.and("thermalMonitor", thermalMonitor)

        return applyFirstMaxToQuery(
            find(
                queryBuilder.toString(),
                /**
                 * This sorting is critical because the policies must be triggered in the correct order during incidents
                 */
                Sort.ascending("priority").and("escalationDelaySeconds"),
                parameters
            ),
            firstIndex = first,
            maxResults = max
        )
    }

    /**
     * List all policies in the database that belong to a given contact
     *
     * @param pagingPolicyContact
     */
    suspend fun listAllByContact(
        pagingPolicyContact: PagingPolicyContactEntity
    ): List<ThermalMonitorPagingPolicyEntity> {
        val queryBuilder = StringBuilder()
        val parameters = Parameters()

        addCondition(queryBuilder, "pagingPolicyContact = :pagingPolicyContact")
        parameters.and("pagingPolicyContact", pagingPolicyContact)

        return find(queryBuilder.toString(), parameters).list<ThermalMonitorPagingPolicyEntity>().awaitSuspending()
    }

    /**
     * List all policies in the database that belong to a given contact
     *
     * @param thermalMonitor
     */
    suspend fun listAllByMonitor(
        thermalMonitor: ThermalMonitorEntity
    ): List<ThermalMonitorPagingPolicyEntity> {
        val queryBuilder = StringBuilder()
        val parameters = Parameters()

        addCondition(queryBuilder, "thermalMonitor = :thermalMonitor")
        parameters.and("thermalMonitor", thermalMonitor)

        return find(queryBuilder.toString(), parameters).list<ThermalMonitorPagingPolicyEntity>().awaitSuspending()
    }

    /**
     * Update policy data in the database
     *
     * @param entityToUpdate
     * @param priority
     * @param escalationDelaySeconds
     * @param pagingPolicyContact
     * @param modifierId
     */
    suspend fun update(
        entityToUpdate: ThermalMonitorPagingPolicyEntity,
        priority: Int,
        escalationDelaySeconds: Int,
        pagingPolicyContact: PagingPolicyContactEntity,
        modifierId: UUID
    ): ThermalMonitorPagingPolicyEntity {
        entityToUpdate.priority = priority
        entityToUpdate.escalationDelaySeconds = escalationDelaySeconds
        entityToUpdate.pagingPolicyContact = pagingPolicyContact
        entityToUpdate.lastModifierId = modifierId

        return persistSuspending(entityToUpdate)
    }
}