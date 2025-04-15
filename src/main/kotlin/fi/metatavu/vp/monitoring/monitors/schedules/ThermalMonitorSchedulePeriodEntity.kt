package fi.metatavu.vp.monitoring.monitors.schedules

import fi.metatavu.vp.monitoring.monitors.ThermalMonitorEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "thermalmonitorscheduleperiod")
class ThermalMonitorSchedulePeriodEntity {
    @Id
    lateinit var id: UUID

    @ManyToOne
    lateinit var thermalMonitor: ThermalMonitorEntity

    @Column(nullable = false)
    var startWeekDay: Int? = null

    @Column(nullable = false)
    var startHour: Int? = null

    @Column(nullable = false)
    var startMinute: Int? = null

    @Column(nullable = false)
    var endWeekDay: Int? = null

    @Column(nullable = false)
    var endHour: Int? = null

    @Column(nullable = false)
    var endMinute: Int? = null
}