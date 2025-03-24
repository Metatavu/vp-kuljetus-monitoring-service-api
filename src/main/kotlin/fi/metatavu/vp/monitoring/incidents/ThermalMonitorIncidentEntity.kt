package fi.metatavu.vp.monitoring.incidents

import fi.metatavu.vp.monitoring.monitors.ThermalMonitorEntity
import fi.metatavu.vp.monitoring.monitors.thermometers.MonitorThermometerEntity
import java.time.OffsetDateTime
import java.util.*
import jakarta.persistence.*

@Entity
@Table(name="thermalmonitorincident")
class ThermalMonitorIncidentEntity {
    @Id
    lateinit var id: UUID

    @Column
    lateinit var status: String

    @Column
    lateinit var triggeredAt: OffsetDateTime

    @Column
    var acknowledgedAt: OffsetDateTime? = null

    @Column
    var resolvedAt: OffsetDateTime? = null

    @Column
    var acknowledgedBy: UUID? = null

    @Column
    var resolvedBy: UUID? = null

    @ManyToOne
    lateinit var monitorThermometer: MonitorThermometerEntity

    @ManyToOne
    lateinit var thermalMonitor: ThermalMonitorEntity

    @Column
    var temperature: Float? = null
}