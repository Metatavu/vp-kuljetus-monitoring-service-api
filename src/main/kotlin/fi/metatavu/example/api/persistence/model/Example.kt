package fi.metatavu.example.api.persistence.model

import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*

/**
 * Entity class for Example
 *
 * @author Jari Nykänen
 */
@Entity
class Example {
    @Id
    var id: UUID? = null

    @Column(nullable = false)
    var name: String? = null

    @Column(nullable = false)
    var amount: Int? = null

    @Column(nullable = false)
    var createdAt: OffsetDateTime? = null

    @Column(nullable = false)
    var modifiedAt: OffsetDateTime? = null

    @Column(nullable = false)
    var creatorId: UUID? = null

    @Column(nullable = false)
    var lastModifierId: UUID? = null

    /**
     * JPA pre-persist event handler
     */
    @PrePersist
    fun onCreate() {
        createdAt = OffsetDateTime.now()
        modifiedAt = OffsetDateTime.now()
    }

    /**
     * JPA pre-update event handler
     */
    @PreUpdate
    fun onUpdate() {
        modifiedAt = OffsetDateTime.now()
    }
}