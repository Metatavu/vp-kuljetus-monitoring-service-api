package fi.metatavu.vp.monitoring.functional.resources

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import fi.metatavu.vp.monitoring.functional.resources.transformers.FindTerminalThermometerResponseTransformer
import fi.metatavu.vp.monitoring.functional.resources.transformers.FindTruckOrTowableThermometerResponseTransformer
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager

class TruckOrTowablelThermometersTestResource: QuarkusTestResourceLifecycleManager {
    private lateinit var wireMockServer: WireMockServer

    override fun start(): Map<String, String> {
        wireMockServer = WireMockServer(WireMockConfiguration().port(8086).extensions(
            FindTruckOrTowableThermometerResponseTransformer()
        ))
        wireMockServer.start()

        wireMockServer.stubFor(
            get(urlPathMatching("/v1/thermometers/.*"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withTransformers(FindTruckOrTowableThermometerResponseTransformer.NAME)
                )
        )

        return mapOf(
            "quarkus.rest-client.\"fi.metatavu.vp.vehiclemanagement.spec.ThermometersApi\".url" to wireMockServer.baseUrl(),
            )

    }

    override fun stop() {
        wireMockServer.stop()
    }
}