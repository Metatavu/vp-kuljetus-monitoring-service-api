package fi.metatavu.vp.monitoring.policies.contacts

import jakarta.persistence.Entity
import jakarta.persistence.Table
import fi.metatavu.vp.monitoring.persistence.Metadata
import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.validation.constraints.NotEmpty
import java.util.*

@Entity
@Table(name = "pagingpolicycontact")
class PagingPolicyContactEntity: Metadata() {
    @Id
    lateinit var id: UUID

    @Column
    @NotEmpty
    lateinit var contactName: String

    @Column
    @NotEmpty
    lateinit var contact: String

    @Column
    @NotEmpty
    lateinit var contactType: String

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID
}