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
    JsonSubTypes.Type(value = CreateLostSensorIncidentsEvent::class, name = "CREATE_LOST_SENSOR_INCIDENTS")
)
data class CreateLostSensorIncidentsEvent(val type: String = "CREATE_LOST_SENSOR_INCIDENTS")