package fi.metatavu.vp.monitoring.functional

import fi.metatavu.jaxrs.test.functional.builder.AbstractAccessTokenTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication
import fi.metatavu.jaxrs.test.functional.builder.auth.KeycloakAccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.NullAccessTokenProvider
import fi.metatavu.vp.monitoring.functional.auth.TestBuilderAuthentication
import fi.metatavu.vp.monitoring.functional.settings.ApiTestSettings
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import org.eclipse.microprofile.config.ConfigProvider

/**
 * Abstract test builder class
 *
 * @author Jari Nykänen
 * @author Antti Leppä
 */
class TestBuilder(private val config: Map<String, String>): AbstractAccessTokenTestBuilder<ApiClient>() {

    var user = createTestBuilderAuthentication(username = "user", password = "userPassword")
    val manager = createTestBuilderAuthentication(username = "manager", password = "managerPassword")

    override fun createTestBuilderAuthentication(
        abstractTestBuilder: AbstractTestBuilder<ApiClient, AccessTokenProvider>,
        authProvider: AccessTokenProvider
    ): AuthorizedTestBuilderAuthentication<ApiClient, AccessTokenProvider> {
        return TestBuilderAuthentication(this, authProvider)
    }

    /**
     * Returns authentication with cron key
     *
     * @param cronKey cron task key
     * @return authorized client
     */
    fun setCronKey(cronKey: String? = null): TestBuilderAuthentication {
        val key = cronKey ?: ApiTestSettings.CRON_API_KEY
        return TestBuilderAuthentication(this, NullAccessTokenProvider(), cronKey = key)
    }

    /**
     * Creates test builder authenticatior for given user
     *
     * @param username username
     * @param password password
     * @return test builder authenticatior for given user
     */
    private fun createTestBuilderAuthentication(username: String, password: String): TestBuilderAuthentication {
        val serverUrl = ConfigProvider.getConfig().getValue("quarkus.oidc.auth-server-url", String::class.java).substringBeforeLast("/").substringBeforeLast("/")
        val realm: String = ConfigProvider.getConfig().getValue("quarkus.keycloak.devservices.realm-name", String::class.java)
        val clientId = "test"
        val clientSecret = "secret"
        return TestBuilderAuthentication(this, KeycloakAccessTokenProvider(serverUrl, realm, clientId, username, password, clientSecret))
    }

}