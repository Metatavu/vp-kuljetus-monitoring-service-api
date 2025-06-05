package fi.metatavu.vp.monitoring.functional.resources

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import fi.metatavu.vp.monitoring.functional.resources.transformers.ListSitesResponseTransformer
import fi.metatavu.vp.monitoring.functional.resources.transformers.ListTowablesResponseTransformer
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager

class TowablesTestResource: QuarkusTestResourceLifecycleManager {
    private lateinit var wireMockServer: WireMockServer

    override fun start(): Map<String, String> {
        wireMockServer = WireMockServer(WireMockConfiguration().port(8085).extensions(
            ListTowablesResponseTransformer()
        ))
        wireMockServer.start()
        wireMockServer.stubFor(
            get(urlPathMatching("/v1/towables.*"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withTransformers(ListTowablesResponseTransformer.NAME)
                )
        )


        return mapOf(
            "quarkus.rest-client.\"fi.metatavu.vp.vehiclemanagement.spec.TowablesApi\".url" to wireMockServer.baseUrl(),
            )

    }

    override fun stop() {
        wireMockServer.stop()
    }
}