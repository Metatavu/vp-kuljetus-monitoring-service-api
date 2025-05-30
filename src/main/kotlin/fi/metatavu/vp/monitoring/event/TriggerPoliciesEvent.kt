package fi.metatavu.vp.monitoring.event

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = TriggerPoliciesEvent::class, name = "TRIGGER_POLICIES")
)
data class TriggerPoliciesEvent(val type: String = "TRIGGER_POLICIES")
