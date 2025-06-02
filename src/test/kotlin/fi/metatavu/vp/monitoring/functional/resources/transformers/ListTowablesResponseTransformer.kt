package fi.metatavu.vp.monitoring.functional.resources.transformers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2
import com.github.tomakehurst.wiremock.http.Response
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import fi.metatavu.vp.monitoring.functional.resources.TestData
import fi.metatavu.vp.vehiclemanagement.model.Towable

class ListTowablesResponseTransformer: ResponseTransformerV2 {
    override fun getName(): String {
        return NAME
    }

    override fun transform(response: Response?, serveEvent: ServeEvent?): Response {
        val queryParams = serveEvent?.request?.queryParams
        val thermometerId = queryParams?.get("thermometerId")?.values()?.firstOrNull()
        val towablesJson = if (thermometerId != null && thermometerId != TestData.truckAndTowableThermometers[1].id!!.toString()) {
            jacksonObjectMapper().writeValueAsString(emptyArray<Towable>())
        } else {
            jacksonObjectMapper().writeValueAsString(TestData.towables)
        }

        return Response.Builder.like(response).but().body(towablesJson).build()
    }

    companion object {
        const val NAME = "list-towables-response-transformer"
    }
}