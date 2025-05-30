package fi.metatavu.vp.monitoring.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.monitoring.functional.TestBuilder
import fi.metatavu.vp.monitoring.functional.settings.ApiTestSettings
import fi.metatavu.vp.test.client.apis.ThermalMonitorIncidentsApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.infrastructure.ClientException
import fi.metatavu.vp.test.client.models.ThermalMonitorIncident
import fi.metatavu.vp.test.client.models.ThermalMonitorIncidentStatus
import org.junit.Assert
import java.util.*

/**
 * Test builder resource for Thermal Monitor Incidents API
 */
class ThermalMonitorIncidentTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    private val cronKey: String?,
    apiClient: ApiClient
) : ApiTestBuilderResource<ThermalMonitorIncident, ApiClient>(testBuilder, apiClient) {

    override fun clean(t: ThermalMonitorIncident) {}

    override fun getApi(): ThermalMonitorIncidentsApi {
        if (cronKey != null) {
            ApiClient.apiKey["X-CRON-Key"] = cronKey
        }

        ApiClient.accessToken = accessTokenProvider?.accessToken
        return ThermalMonitorIncidentsApi(ApiTestSettings.apiBasePath)
    }

    /**
     * List thermal monitor incidents
     *
     * @param monitorId
     * @param thermometerId
     * @param status
     * @param triggeredBefore
     * @param triggeredAfter
     * @param first
     * @param max
     */
    fun listThermalMonitorIncidents(
        monitorId: UUID? = null,
        thermometerId: UUID? = null,
        status: ThermalMonitorIncidentStatus? = null,
        triggeredBefore: String? = null,
        triggeredAfter: String? = null,
        first: Int? = null,
        max: Int? = null
    ): Array<ThermalMonitorIncident> {
        return api.listThermalMonitorIncidents(
            monitorId = monitorId,
            thermometerId = thermometerId,
            incidentStatus = status,
            before = triggeredBefore,
            after = triggeredAfter,
            first = first,
            max = max
        )
    }

    /**
     * Asserts that thermal monitor incident list fails with expected status
     *
     * @param expectedStatus expected status
     */
    fun assertListIncidentsFail(
        expectedStatus: Int
    ) {
        try {
            listThermalMonitorIncidents()
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Updates an incident
     *
     * @param id
     * @param thermalMonitorIncident
     */
    fun update(id: UUID, thermalMonitorIncident: ThermalMonitorIncident): ThermalMonitorIncident {
        return api.updateThermalMonitorIncident(id, thermalMonitorIncident)
    }

    /**
     * Asserts that incident update fails with expected status
     *
     * @param expectedStatus
     * @param id
     * @param thermalMonitorIncident
     */
    fun assertUpdateFail(expectedStatus: Int, id: UUID, thermalMonitorIncident: ThermalMonitorIncident) {
        try {
            update(id, thermalMonitorIncident)
            Assert.fail(String.format("Expected update to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Trigger incidents for lost sensors
     */
    fun createSensorLostIncidents() {
        return api.createLostSensorIncidents()
    }
}