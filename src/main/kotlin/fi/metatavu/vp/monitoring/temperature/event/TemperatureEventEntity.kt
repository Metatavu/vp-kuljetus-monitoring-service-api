package fi.metatavu.vp.monitoring.temperature.event

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

/**
 * Saved temperature event
 */
@Entity
@Table(name = "temperatureevent")
class TemperatureEventEntity {
    @Id
    lateinit var id: UUID

    @Column
    lateinit var sensorId: String

    @Column(nullable = false)
    var timestamp: Long? = null

    @Column(nullable = false)
    var temperature: Float? = null
}