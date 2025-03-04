package fi.metatavu.vp.monitoring.monitors

import fi.metatavu.vp.api.model.ThermalMonitor
import fi.metatavu.vp.monitoring.monitors.thermometers.MonitorThermometerRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.UUID

@ApplicationScoped
class ThermalMonitorController {
    @Inject
    lateinit var thermalMonitorRepository: ThermalMonitorRepository

    @Inject
    lateinit var monitorThermometerRepository: MonitorThermometerRepository

    /**
     * Create a thermal monitor to monitor for incidents
     *
     * @param thermalMonitor
     * @param creatorId
     */
    suspend fun create(thermalMonitor: ThermalMonitor, creatorId: UUID): ThermalMonitorEntity {
        val monitor = thermalMonitorRepository.create(
            name = thermalMonitor.name,
            status = thermalMonitor.status.toString(),
            creatorId = creatorId,
            thresholdLow = thermalMonitor.lowerThresholdTemperature,
            thresholdHigh = thermalMonitor.upperThresholdTemperature,
            activeFrom = thermalMonitor.activeFrom,
            activeTo = thermalMonitor.activeTo
        )

        thermalMonitor.thermometerIds.forEach {
            monitorThermometerRepository.create(thermometerId = it, thermalMonitorEntity = monitor, creatorId = creatorId)
        }

        return monitor
    }

    /**
     * Find thermal monitor
     *
     * @param id monitor id
     */
    suspend fun find(id: UUID): ThermalMonitorEntity? {
        return thermalMonitorRepository.findByIdSuspending(id)
    }

    /**
     * Delete thermal monitor
     *
     * @param thermalMonitorEntity
     */
    suspend fun delete(thermalMonitorEntity: ThermalMonitorEntity) {
        monitorThermometerRepository.list(thermalMonitorEntity).forEach {
            monitorThermometerRepository.deleteSuspending(it)
        }

        thermalMonitorRepository.deleteSuspending(thermalMonitorEntity)
    }
}