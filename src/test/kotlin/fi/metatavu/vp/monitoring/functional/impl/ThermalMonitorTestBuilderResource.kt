package fi.metatavu.vp.monitoring.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.monitoring.functional.TestBuilder
import fi.metatavu.vp.monitoring.functional.settings.ApiTestSettings
import fi.metatavu.vp.test.client.apis.ThermalMonitorsApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.infrastructure.ClientException
import fi.metatavu.vp.test.client.models.ThermalMonitor
import fi.metatavu.vp.test.client.models.ThermalMonitorStatus
import org.junit.Assert
import java.time.OffsetDateTime
import java.util.*

/**
 * Test builder resource for Thermal monitors API
 */
class ThermalMonitorTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<ThermalMonitor, ApiClient>(testBuilder, apiClient) {

    override fun clean(t: ThermalMonitor) {
        api.deleteThermalMonitor(t.id!!)
    }

    override fun getApi(): ThermalMonitorsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return ThermalMonitorsApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Creates new monitor
     *
     * @param thermalMonitor
     */
    fun create(thermalMonitor: ThermalMonitor): ThermalMonitor {
        return addClosable(api.createThermalMonitor(thermalMonitor))
    }

    /**
     * Asserts that monitor creation fails with expected status
     *
     * @param expectedStatus expected status
     * @param thermalMonitor monitor
     */
    fun assertCreateFail(expectedStatus: Int, thermalMonitor: ThermalMonitor) {
        try {
            create(thermalMonitor)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Finds thermal monitor
     *
     * @param id id
     * @return found thermal monitor
     */
    fun findThermalMonitor(id: UUID): ThermalMonitor {
        return api.findThermalMonitor(id)
    }

    /**
     * Asserts that thermal monitor find fails with expected status
     *
     * @param expectedStatus expected status
     * @param id id
     */
    fun assertFindMonitorFail(expectedStatus: Int, id: UUID) {
        try {
            findThermalMonitor(id)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Deletes thermal monitor
     *
     * @param id
     */
    fun deleteThermalMonitor(id: UUID) {
        api.deleteThermalMonitor(id)
        removeCloseable { closable: Any ->
            if (closable !is ThermalMonitor) {
                return@removeCloseable false
            }

            closable.id == id
        }
    }

    /**
     * Asserts that monitor deletion fails with expected status
     *
     * @param id
     * @param expectedStatus expected status
     */
    fun assertDeleteMonitorFail(expectedStatus: Int, id: UUID) {
        try {
            deleteThermalMonitor(id)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * List thermal monitors
     *
     * @param status
     * @param activeBefore
     * @param activeAfter
     * @param first
     * @param max
     */
    fun listThermalMonitors(status: ThermalMonitorStatus?, activeBefore: String?, activeAfter: String?, first: Int?, max: Int?): Array<ThermalMonitor> {
        return api.listThermalMonitors(status = status, activeBefore = activeBefore, activeAfter = activeAfter, first = first, max = max)
    }

    /**
     * Asserts that thermal monitor find fails with expected status
     *
     * @param expectedStatus expected status
     */
    fun assertListMonitorFail(expectedStatus: Int) {
        try {
            listThermalMonitors(null, null, null, null, null)
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Updates a monitor
     *
     * @param id
     * @param thermalMonitor
     */
    fun update(id: UUID, thermalMonitor: ThermalMonitor): ThermalMonitor {
        return api.updateThermalMonitor(id, thermalMonitor)
    }

    /**
     * Asserts that monitor update fails with expected status
     *
     * @param expectedStatus
     * @param id
     * @param thermalMonitor
     */
    fun assertUpdateFail(expectedStatus: Int, id: UUID, thermalMonitor: ThermalMonitor) {
        try {
            update(id, thermalMonitor)
            Assert.fail(String.format("Expected update to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }
}