package fi.metatavu.vp.monitoring.policies.contacts

import jakarta.persistence.Entity
import jakarta.persistence.Table
import fi.metatavu.vp.monitoring.persistence.Metadata
import jakarta.persistence.Column
import jakarta.persistence.Id
import java.util.*

@Entity
@Table(name = "pagingpolicycontact")
class PagingPolicyContactEntity: Metadata() {
    @Id
    lateinit var id: UUID

    @Column
    var contactName: String? = null

    @Column
    var email: String? = null

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID
}