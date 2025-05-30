package fi.metatavu.vp.monitoring.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.test.client.models.ThermalMonitorPagingPolicy
import fi.metatavu.vp.monitoring.functional.TestBuilder
import fi.metatavu.vp.monitoring.functional.settings.ApiTestSettings
import fi.metatavu.vp.test.client.apis.ThermalMonitorPagingPoliciesApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.infrastructure.ClientException
import org.junit.Assert
import java.util.*

/**
 * Test builder resource for thermal monitor paging policies API
 */
class ThermalMonitorPagingPolicyTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    private val cronKey: String?,
    apiClient: ApiClient
) : ApiTestBuilderResource<ThermalMonitorPagingPolicy, ApiClient>(testBuilder, apiClient) {

    override fun clean(t: ThermalMonitorPagingPolicy) {
        api.deletePagingPolicy(t.thermalMonitorId, t.id!!)
    }

    override fun getApi(): ThermalMonitorPagingPoliciesApi {
        if (cronKey != null) {
            ApiClient.apiKey["X-CRON-Key"] = cronKey
        }

        ApiClient.accessToken = accessTokenProvider?.accessToken
        return ThermalMonitorPagingPoliciesApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Creates new thermal monitor paging policy
     *
     * @param thermalMonitorId
     * @param policy
     */
    fun create(thermalMonitorId: UUID, policy: ThermalMonitorPagingPolicy): ThermalMonitorPagingPolicy {
        return addClosable(api.createPagingPolicy(thermalMonitorId = thermalMonitorId, thermalMonitorPagingPolicy = policy))
    }

    /**
     * Asserts that thermal monitor paging policy creation fails with expected status
     *
     * @param expectedStatus
     * @param policy
     */
    fun assertCreateFailStatus(expectedStatus: Int, thermalMonitorId: UUID, policy: ThermalMonitorPagingPolicy) {
        try {
            create(thermalMonitorId = thermalMonitorId, policy = policy)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Finds thermal monitor paging policy
     *
     * @param thermalMonitorId
     * @param id
     */
    fun findThermalMonitorPagingPolicy(thermalMonitorId: UUID, id: UUID): ThermalMonitorPagingPolicy {
        return api.findPagingPolicy(thermalMonitorId, id)
    }

    /**
     * Asserts that thermal monitor paging policy find fails with expected status
     *
     * @param expectedStatus expected status
     * @param thermalMonitorId
     * @param id id
     */
    fun assertFindFailStatus(expectedStatus: Int, thermalMonitorId: UUID, id: UUID) {
        try {
            findThermalMonitorPagingPolicy(thermalMonitorId = thermalMonitorId, id = id)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Deletes thermal monitor paging policy
     *
     * @param thermalMonitorId
     * @param id
     */
    fun deleteThermalMonitorPagingPolicy(thermalMonitorId: UUID, id: UUID) {
        api.deletePagingPolicy(thermalMonitorId, id)
        removeCloseable { closable: Any ->
            if (closable !is ThermalMonitorPagingPolicy) {
                return@removeCloseable false
            }

            closable.id == id
        }
    }

    /**
     * Asserts that thermal monitor paging policy deletion fails with expected status
     *
     * @param thermalMonitorId
     * @param id
     * @param expectedStatus
     */
    fun assertDeleteFailStatus(expectedStatus: Int, thermalMonitorId: UUID, id: UUID) {
        try {
            deleteThermalMonitorPagingPolicy(thermalMonitorId = thermalMonitorId, id = id)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * List thermal monitor paging policies
     *
     * @param thermalMonitorId
     * @param first
     * @param max
     */
    fun listThermalMonitorPagingPolicies(thermalMonitorId: UUID, first: Int, max: Int): Array<ThermalMonitorPagingPolicy> {
        return api.listPagingPolicies(thermalMonitorId = thermalMonitorId, first = first, max = max)
    }

    /**
     * Asserts that thermal monitor paging policy list fails with expected status
     *
     * @param expectedStatus expected status
     */
    fun assertListFailStatus(expectedStatus: Int, thermalMonitorId: UUID, first: Int, max: Int) {
        try {
            listThermalMonitorPagingPolicies(thermalMonitorId = thermalMonitorId, first = first, max = max)
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Updates a thermal monitor paging policy
     *
     * @param thermalMonitorId
     * @param id
     * @param thermalMonitorPagingPolicy
     */
    fun update(thermalMonitorId: UUID, id: UUID, thermalMonitorPagingPolicy: ThermalMonitorPagingPolicy): ThermalMonitorPagingPolicy {
        return api.updatePagingPolicy(thermalMonitorId, id, thermalMonitorPagingPolicy)
    }

    /**
     * Asserts that thermal monitor paging policy update fails with expected status
     *
     * @param expectedStatus
     * @param thermalMonitorId
     * @param id
     * @param thermalMonitorPagingPolicy
     */
    fun assertUpdateFailStatus(expectedStatus: Int, thermalMonitorId: UUID, id: UUID, thermalMonitorPagingPolicy: ThermalMonitorPagingPolicy) {
        try {
            update(thermalMonitorId, id, thermalMonitorPagingPolicy)
            Assert.fail(String.format("Expected update to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Triggers policies that are due
     */
    fun triggerPolicies() {
        api.triggerPolicies()
    }
}