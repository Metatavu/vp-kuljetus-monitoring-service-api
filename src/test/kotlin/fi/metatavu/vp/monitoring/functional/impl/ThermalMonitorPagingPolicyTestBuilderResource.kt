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
 * Test builder resource for Thermal monitor paging policies API
 */
class ThermalMonitorPagingPolicyTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<ThermalMonitorPagingPolicy, ApiClient>(testBuilder, apiClient) {

    override fun clean(t: ThermalMonitorPagingPolicy) {
        api.deletePagingPolicy(t.id!!)
    }

    override fun getApi(): ThermalMonitorPagingPoliciesApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return ThermalMonitorPagingPoliciesApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Creates new thermal monitor paging policy
     *
     * @param policy
     */
    fun create(policy: ThermalMonitorPagingPolicy): ThermalMonitorPagingPolicy {
        return addClosable(api.createThermalMonitorPagingPolicy(thermalMonitorPagingPolicy = policy))
    }

    /**
     * Asserts that thermal monitor paging policy creation fails with expected status
     *
     * @param expectedStatus
     * @param policy
     */
    fun assertCreateFail(expectedStatus: Int, policy: ThermalMonitorPagingPolicy) {
        try {
            create(policy)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Finds thermal monitor paging policy
     *
     * @param id id
     * @return found thermal monitor paging policy
     */
    fun findThermalMonitorPagingPolicy(id: UUID): ThermalMonitorPagingPolicy {
        return api.findThermalMonitorPagingPolicy(id)
    }

    /**
     * Asserts that thermal monitor paging policy find fails with expected status
     *
     * @param expectedStatus expected status
     * @param id id
     */
    fun assertFindThermalMonitorPagingPolicyFail(expectedStatus: Int, id: UUID) {
        try {
            findThermalMonitorPagingPolicy(id)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Deletes thermal monitor paging policy
     *
     * @param id
     */
    fun deleteThermalMonitorPagingPolicy(id: UUID) {
        api.deleteThermalMonitorPagingPolicy(id)
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
     * @param id
     * @param expectedStatus expected status
     */
    fun assertDeleteThermalMonitorPagingPolicyFail(expectedStatus: Int, id: UUID) {
        try {
            deleteThermalMonitorPagingPolicy(id)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * List thermal monitor paging policies
     *
     * @param first
     * @param max
     */
    fun listThermalMonitorPagingPolicies(first: Int?, max: Int?): Array<ThermalMonitorPagingPolicy> {
        return api.listThermalMonitorPagingPolicies(first, max)
    }

    /**
     * Asserts that thermal monitor paging policy list fails with expected status
     *
     * @param expectedStatus expected status
     */
    fun assertListThermalMonitorPagingPoliciesFail(expectedStatus: Int) {
        try {
            listThermalMonitorPagingPolicies(null, null)
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Updates a thermal monitor paging policy
     *
     * @param id
     * @param thermalMonitorPagingPolicy
     */
    fun update(id: UUID, thermalMonitorPagingPolicy: ThermalMonitorPagingPolicy): ThermalMonitorPagingPolicy {
        return api.updateThermalMonitorPagingPolicy(id, thermalMonitorPagingPolicy)
    }

    /**
     * Asserts that thermal monitor paging policy update fails with expected status
     *
     * @param expectedStatus
     * @param id
     * @param thermalMonitorPagingPolicy
     */
    fun assertUpdateFail(expectedStatus: Int, id: UUID, thermalMonitorPagingPolicy: ThermalMonitorPagingPolicy) {
        try {
            update(id, thermalMonitorPagingPolicy)
            Assert.fail(String.format("Expected update to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }
}