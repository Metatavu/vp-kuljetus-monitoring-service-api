package fi.metatavu.vp.monitoring.monitors.thermometers

import fi.metatavu.vp.monitoring.incidents.IncidentController
import fi.metatavu.vp.monitoring.monitors.ThermalMonitorEntity
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

@ApplicationScoped
class MonitorThermometerController {
    @Inject
    lateinit var monitorThermometerRepository: MonitorThermometerRepository

    @Inject
    lateinit var incidentController: IncidentController

    /**
     * Save thermal monitor to the database
     *
     * @param thermometerId
     * @param thermalMonitorEntity
     * @param creatorId
     */
    suspend fun create(thermometerId: UUID, thermalMonitorEntity: ThermalMonitorEntity, creatorId: UUID): MonitorThermometerEntity {
        return monitorThermometerRepository.create(
            thermometerId = thermometerId,
            thermalMonitorEntity = thermalMonitorEntity,
            creatorId = creatorId
        )
    }

    /**
     * Lists thermometers
     *
     *  @param thermalMonitorEntity
     *  @param thermometerId
     *  @param onlyActive
     */
    suspend fun listThermometers(
        thermalMonitorEntity: ThermalMonitorEntity? = null,
        thermometerId: UUID? = null,
        onlyActive: Boolean = false,
        includeArchived: Boolean = false): List<MonitorThermometerEntity> {
        return monitorThermometerRepository.listThermometers(
            thermalMonitorEntity = thermalMonitorEntity,
            thermometerId = thermometerId,
            onlyActive = onlyActive,
            includeArchived = includeArchived
        )
    }

    /**
     * Remove monitor thermometer from the  database
     *
     * @param monitorThermometerEntity
     */
    suspend fun delete(monitorThermometerEntity: MonitorThermometerEntity) {
        incidentController.list(monitorThermometer = monitorThermometerEntity).forEach {

            incidentController.delete(it)
        }

        monitorThermometerRepository.deleteSuspending(monitorThermometerEntity)
    }

    /**
     * Update the time information about when this thermometer sent the latest measurement
     * This is done when a new temperature event is received
     *
     * @param monitorThermometerEntity
     */
    suspend fun updateThermometerLastMeasuredAt(monitorThermometerEntity: MonitorThermometerEntity, lastMeasuredAt: Long): MonitorThermometerEntity {
        return monitorThermometerRepository.updateThermometerLastMeasuredAt(
            monitorThermometerEntity,
            lastMeasuredAt
        )
    }

    /**
     * Archives a thermometer
     * This is done instead of deleting to keep the incident history
     *
     * @param monitorThermometerEntity
     * @param lastModifierId
     */
    suspend fun archiveThermometer(monitorThermometerEntity: MonitorThermometerEntity, lastModifierId: UUID) {
        monitorThermometerRepository.archiveThermometer(
            monitorThermometerEntity = monitorThermometerEntity,
            lastModifierId = lastModifierId
        )
    }
}