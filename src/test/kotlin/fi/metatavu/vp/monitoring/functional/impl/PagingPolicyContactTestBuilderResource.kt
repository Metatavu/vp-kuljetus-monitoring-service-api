package fi.metatavu.vp.monitoring.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.test.client.models.PagingPolicyContact
import fi.metatavu.vp.monitoring.functional.TestBuilder
import fi.metatavu.vp.monitoring.functional.settings.ApiTestSettings
import fi.metatavu.vp.test.client.apis.PagingPolicyContactsApi
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
class PagingPolicyContactTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<PagingPolicyContact, ApiClient>(testBuilder, apiClient) {

    override fun clean(t: PagingPolicyContact) {
        api.deletePagingPolicyContact(t.id!!)
    }

    override fun getApi(): PagingPolicyContactsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return PagingPolicyContactsApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Creates new paging policy contact
     *
     * @param contact
     */
    fun create(contact: PagingPolicyContact): PagingPolicyContact {
        return addClosable(api.createPagingPolicyContact(pagingPolicyContact = contact))
    }

    /**
     * Asserts that paging policy contact creation fails with expected status
     *
     * @param expectedStatus
     * @param contact
     */
    fun assertCreateFail(expectedStatus: Int, contact: PagingPolicyContact) {
        try {
            create(contact)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Finds paging policy contact
     *
     * @param id id
     * @return found paging policy contact
     */
    fun findPagingPolicyContact(id: UUID): PagingPolicyContact {
        return api.findPagingPolicyContact(id)
    }

    /**
     * Asserts that paging policy contact find fails with expected status
     *
     * @param expectedStatus expected status
     * @param id id
     */
    fun assertFindPagingPolicyContactFail(expectedStatus: Int, id: UUID) {
        try {
            findPagingPolicyContact(id)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Deletes paging policy contact
     *
     * @param id
     */
    fun deletePagingPolicyContact(id: UUID) {
        api.deletePagingPolicyContact(id)
        removeCloseable { closable: Any ->
            if (closable !is PagingPolicyContact) {
                return@removeCloseable false
            }

            closable.id == id
        }
    }

    /**
     * Asserts that paging policy contact deletion fails with expected status
     *
     * @param id
     * @param expectedStatus expected status
     */
    fun assertDeletePagingPolicyContactFail(expectedStatus: Int, id: UUID) {
        try {
            deletePagingPolicyContact(id)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }
}