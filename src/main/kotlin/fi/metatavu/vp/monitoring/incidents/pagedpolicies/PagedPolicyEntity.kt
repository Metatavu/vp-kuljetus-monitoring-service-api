package fi.metatavu.vp.monitoring.incidents.pagedpolicies

import fi.metatavu.vp.monitoring.incidents.ThermalMonitorIncidentEntity
import fi.metatavu.vp.monitoring.policies.ThermalMonitorPagingPolicyEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

/**
 * This table tracks policies that have been triggered for each incident
 */
@Entity
@Table(name = "pagedpolicy")
class PagedPolicyEntity {
    @Id
    lateinit var id: UUID

    @ManyToOne
    lateinit var policy: ThermalMonitorPagingPolicyEntity

    @ManyToOne
    lateinit var incident: ThermalMonitorIncidentEntity

    @Column
    lateinit var createdAt: OffsetDateTime
}