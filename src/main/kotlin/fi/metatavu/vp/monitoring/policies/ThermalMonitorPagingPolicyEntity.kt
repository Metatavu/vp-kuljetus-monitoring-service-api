package fi.metatavu.vp.monitoring.policies

import fi.metatavu.vp.monitoring.monitors.ThermalMonitorEntity
import fi.metatavu.vp.monitoring.persistence.Metadata
import fi.metatavu.vp.monitoring.policies.contacts.PagingPolicyContactEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotEmpty
import java.util.*

@Entity
@Table(name = "thermalmonitorpagingpolicy")
class ThermalMonitorPagingPolicyEntity: Metadata() {
    @Id
    lateinit var id: UUID

    @Column
    @NotEmpty
    lateinit var policyType: String

    @Column(nullable = false)
    var priority: Int? = null

    @Column(nullable = false)
    var escalationDelaySeconds: Int? = null

    @ManyToOne
    lateinit var pagingPolicyContact: PagingPolicyContactEntity

    @ManyToOne
    lateinit var thermalMonitor: ThermalMonitorEntity

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID
}