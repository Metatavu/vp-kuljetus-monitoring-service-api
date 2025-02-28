package fi.metatavu.example.api.test.functional.auth

import fi.metatavu.example.client.infrastructure.ApiClient
import fi.metatavu.example.api.test.functional.TestBuilder
import fi.metatavu.example.api.test.functional.impl.ExamplesTestBuilderResource
import fi.metatavu.example.api.test.functional.settings.ApiTestSettings
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenTestBuilderAuthentication


/**
 * Test builder authentication
 *
 * @author Jari Nykänen
 * @author Antti Leppä
 *
 * @param testBuilder test builder instance
 * @param accessTokenProvider access token provider
 */
class TestBuilderAuthentication(
    private val testBuilder: TestBuilder,
    accessTokenProvider: AccessTokenProvider
): AccessTokenTestBuilderAuthentication<ApiClient>(testBuilder, accessTokenProvider) {

    private var accessTokenProvider: AccessTokenProvider? = accessTokenProvider

    var examples: ExamplesTestBuilderResource = ExamplesTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())

    override fun createClient(authProvider: AccessTokenProvider): ApiClient {
        val result = ApiClient(ApiTestSettings.apiBasePath)
        ApiClient.accessToken = authProvider.accessToken
        return result
    }

}