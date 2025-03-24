package fi.metatavu.vp.monitoring.functional.auth

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenTestBuilderAuthentication
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.monitoring.functional.TestBuilder
import fi.metatavu.vp.monitoring.functional.impl.PagingPolicyContactTestBuilderResource
import fi.metatavu.vp.monitoring.functional.impl.ThermalMonitorIncidentTestBuilderResource
import fi.metatavu.vp.monitoring.functional.impl.ThermalMonitorPagingPolicyTestBuilderResource
import fi.metatavu.vp.monitoring.functional.impl.ThermalMonitorTestBuilderResource
import fi.metatavu.vp.monitoring.functional.settings.ApiTestSettings

/**
 * Test builder authentication
 *
 * @author Jari Nykänen
 * @author Antti Leppä
 *
 * @param testBuilder test builder instance
 * @param accessTokenProvider access token provider
 * @param cronKey cron key
 */
class TestBuilderAuthentication(
    private val testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider,
    private val cronKey: String? = null
) : AccessTokenTestBuilderAuthentication<ApiClient>(testBuilder, accessTokenProvider) {

    val thermalMonitors = ThermalMonitorTestBuilderResource(testBuilder, accessTokenProvider, cronKey, createClient(accessTokenProvider))
    val pagingPolicyContacts = PagingPolicyContactTestBuilderResource(testBuilder, accessTokenProvider, createClient(accessTokenProvider))
    val thermalMonitorPagingPolicies = ThermalMonitorPagingPolicyTestBuilderResource(testBuilder, accessTokenProvider, cronKey, createClient(accessTokenProvider))
    val incidents = ThermalMonitorIncidentTestBuilderResource(testBuilder, accessTokenProvider, createClient(accessTokenProvider))

    override fun createClient(authProvider: AccessTokenProvider): ApiClient {
        val result = ApiClient(ApiTestSettings.apiBasePath)
        ApiClient.accessToken = authProvider.accessToken
        return result
    }

}