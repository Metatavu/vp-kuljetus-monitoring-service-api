package fi.metatavu.vp.monitoring.monitors

import fi.metatavu.vp.api.model.ThermalMonitor
import fi.metatavu.vp.api.model.ThermalMonitorStatus
import fi.metatavu.vp.monitoring.monitors.thermometers.MonitorThermometerRepository
import fi.metatavu.vp.usermanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class ThermalMonitorTranslator: AbstractTranslator<ThermalMonitorEntity, ThermalMonitor>() {
    @Inject
    lateinit var monitorThermometerRepository: MonitorThermometerRepository

    /**
     * Translate thermal monitor
     *
     * @param entity thermal monitor
     */
    override suspend fun translate(entity: ThermalMonitorEntity): ThermalMonitor {
        return ThermalMonitor(
            name = entity.name,
            status = ThermalMonitorStatus.valueOf(entity.status),
            thermometerIds = monitorThermometerRepository.list(entity).map { it.thermometerId },
            id = entity.id,
            lowerThresholdTemperature = entity.thresholdLow,
            upperThresholdTemperature = entity.thresholdHigh,
            activeTo = entity.activeTo,
            activeFrom = entity.activeFrom,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt
        )
    }
}