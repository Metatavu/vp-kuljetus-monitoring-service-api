package fi.metatavu.vp.monitoring.monitors

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.*
import fi.metatavu.vp.monitoring.persistence.Metadata

/**
 * This entity is used to set alarm thresholds for a set of thermometers
 */
@Entity
@Table(name = "thermalmonitor")
class ThermalMonitorEntity: Metadata() {
    @Id
    lateinit var id: UUID

    @Column
    lateinit var name: String

    @Column
    lateinit var status: String

    @Column
    var thresholdLow: Float? = null

    @Column
    var thresholdHigh: Float? = null

    @Column
    var activeFrom: OffsetDateTime? = null

    @Column
    var activeTo: OffsetDateTime? = null

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID
}