package fi.metatavu.vp.monitoring.functional.resources.transformers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2
import com.github.tomakehurst.wiremock.http.Response
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import fi.metatavu.vp.monitoring.functional.resources.TestData

class FindTruckOrTowableThermometerResponseTransformer: ResponseTransformerV2 {
    override fun getName(): String {
        return NAME
    }

    override fun transform(response: Response?, serveEvent: ServeEvent?): Response {
        val thermometerId = serveEvent?.request?.absoluteUrl?.split("/")!!.last()
        val thermometerJson = jacksonObjectMapper().writeValueAsString(TestData.truckAndTowableThermometers.find { thermometerId == it.id.toString() })

        return Response.Builder.like(response).but().body(thermometerJson).build()
    }

    companion object {
        const val NAME = "find-truck-or-towable-response-transformer"
    }
}