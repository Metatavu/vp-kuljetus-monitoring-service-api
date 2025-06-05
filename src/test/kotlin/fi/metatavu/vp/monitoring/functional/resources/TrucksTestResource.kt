package fi.metatavu.vp.monitoring.functional.resources

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import fi.metatavu.vp.monitoring.functional.resources.transformers.ListSitesResponseTransformer
import fi.metatavu.vp.monitoring.functional.resources.transformers.ListTrucksResponseTransformer
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager

class TrucksTestResource: QuarkusTestResourceLifecycleManager {
    private lateinit var wireMockServer: WireMockServer

    override fun start(): Map<String, String> {
        wireMockServer = WireMockServer(WireMockConfiguration().port(8084).extensions(
            ListTrucksResponseTransformer()
        ))
        wireMockServer.start()
        wireMockServer.stubFor(
            get(urlPathMatching("/v1/trucks.*"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withTransformers(ListTrucksResponseTransformer.NAME)
                )
        )


        return mapOf(
            "quarkus.rest-client.\"fi.metatavu.vp.vehiclemanagement.spec.TrucksApi\".url" to wireMockServer.baseUrl(),
            )

    }

    override fun stop() {
        wireMockServer.stop()
    }
}