package fi.metatavu.vp.monitoring.functional.resources

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import fi.metatavu.vp.monitoring.functional.settings.ApiTestSettings
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.apache.commons.codec.binary.Base64

/**
 * Resource for wiremock container
 */
class MailgunTestResource : QuarkusTestResourceLifecycleManager {
    private lateinit var wireMockServer: WireMockServer

    private val domain = ApiTestSettings.MAILGUN_DOMAIN
    private val path = ApiTestSettings.MAILGUN_API_URL_ENDING
    private val key = ApiTestSettings.MAILGUN_API_KEY
    private val sender = ApiTestSettings.MAILGUN_SENDER_EMAIL

    override fun start(): Map<String, String> {
        val config: MutableMap<String, String> = HashMap()
        wireMockServer = WireMockServer()
        wireMockServer.start()
        mailgun()
        config["fi.metatavu.vp.monitoring.mailgun.apiurl"] = wireMockServer.baseUrl() + '/' + path
        config["fi.metatavu.vp.monitoring.mailgun.domain"] = domain
        config["fi.metatavu.vp.monitoring.mailgun.apikey"] = key
        config["fi.metatavu.vp.monitoring.mailgun.sender.email"] = sender
        return config
    }

    override fun stop() {
        wireMockServer.stop()
    }

    /**
     * Sets up mailgun stubs
     */
    private fun mailgun() {
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/$path/$domain/messages"))
            .withHeader("Authorization", WireMock.equalTo(String.format("Basic %s", Base64.encodeBase64String(String.format("api:%s", key).toByteArray()))))
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(WireMock.aResponse().withStatus(200)))
    }

}