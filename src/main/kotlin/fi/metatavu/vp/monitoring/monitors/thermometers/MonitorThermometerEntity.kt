package fi.metatavu.vp.monitoring.monitors.thermometers

import fi.metatavu.vp.monitoring.persistence.Metadata
import fi.metatavu.vp.monitoring.monitors.ThermalMonitorEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import org.jetbrains.annotations.NotNull
import java.util.*

/**
 * Connects a thermometer with a monitor
 */
@Entity
@Table(name = "monitorthermometer")
class MonitorThermometerEntity: Metadata() {
    @Id
    lateinit var id: UUID

    @Column
    lateinit var thermometerId: UUID

    @ManyToOne
    lateinit var thermalMonitor: ThermalMonitorEntity

    @Column
    var lastMeasuredAt: Long? = null

    @Column
    @NotNull
    var archived: Boolean? = null

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID
}