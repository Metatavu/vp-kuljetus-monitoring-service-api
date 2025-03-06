package fi.metatavu.vp.monitoring.monitors.thermometers

import fi.metatavu.vp.monitoring.monitors.ThermalMonitorEntity
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

@ApplicationScoped
class MonitorThermometerController {
    @Inject
    lateinit var monitorThermometerRepository: MonitorThermometerRepository

    /**
     * Save thermal monitor to the database
     *
     * @param thermometerId
     * @param thermalMonitorEntity
     * @param creatorId
     */
    suspend fun create(thermometerId: UUID, thermalMonitorEntity: ThermalMonitorEntity, creatorId: UUID): MonitorThermometerEntity {
        return monitorThermometerRepository.create(thermometerId = thermometerId, thermalMonitorEntity = thermalMonitorEntity, creatorId = creatorId)
    }

    /**
     * Lists thermometers
     *
     *  @param thermalMonitorEntity
     *  @param thermometerId
     */
    suspend fun listThermometers(thermalMonitorEntity: ThermalMonitorEntity?, thermometerId: UUID?): List<MonitorThermometerEntity> {
        return monitorThermometerRepository.listThermometers(thermalMonitorEntity = thermalMonitorEntity, thermometerId = thermometerId)
    }

    /**
     * Remove monitor thermometer from the  database
     *
     * @param monitorThermometerEntity
     */
    suspend fun delete(monitorThermometerEntity: MonitorThermometerEntity) {
        monitorThermometerRepository.deleteSuspending(monitorThermometerEntity)
    }

    /**
     * Update the time information about when this thermometer sent the latest measurement
     * This is done when a new temperature event is received
     *
     * @param monitorThermometerEntity
     */
    suspend fun updateThermometerLastMeasuredAt(monitorThermometerEntity: MonitorThermometerEntity, lastMeasuredAt: Long) {
        monitorThermometerRepository.updateThermometerLastMeasuredAt(monitorThermometerEntity, lastMeasuredAt)
    }


}