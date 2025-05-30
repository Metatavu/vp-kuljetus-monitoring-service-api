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
@JsonSubTypes
    (
    JsonSubTypes.Type(value = ResolveMonitorStatusesEvent::class, name = "RESOLVE_MONITOR_STATUSES")
)
data class ResolveMonitorStatusesEvent(val type: String = "RESOLVE_MONITOR_STATUSES")

