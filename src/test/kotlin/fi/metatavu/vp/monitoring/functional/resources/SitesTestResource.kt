package fi.metatavu.vp.monitoring.functional.resources

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import fi.metatavu.vp.monitoring.functional.resources.transformers.ListSitesResponseTransformer
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager

class SitesTestResource: QuarkusTestResourceLifecycleManager {
    private lateinit var wireMockServer: WireMockServer

    override fun start(): Map<String, String> {
        wireMockServer = WireMockServer(WireMockConfiguration().port(8082).extensions(
            ListSitesResponseTransformer()
        ))
        wireMockServer.start()
        wireMockServer.stubFor(
            get(urlPathMatching("/v1/sites.*"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withTransformers(ListSitesResponseTransformer.NAME)
                )
        )


        return mapOf(
            "quarkus.rest-client.\"fi.metatavu.vp.deliveryinfo.spec.SitesApi\".url" to wireMockServer.baseUrl(),
            )

    }

    override fun stop() {
        wireMockServer.stop()
    }
}