package fi.metatavu.vp.monitoring.functional.resources.transformers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2
import com.github.tomakehurst.wiremock.http.Response
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import fi.metatavu.vp.monitoring.functional.resources.TestData
import fi.metatavu.vp.vehiclemanagement.model.Truck

class ListTrucksResponseTransformer: ResponseTransformerV2 {
    override fun getName(): String {
        return NAME
    }

    override fun transform(response: Response?, serveEvent: ServeEvent?): Response {
        val queryParams = serveEvent?.request?.queryParams
        val thermometerId = queryParams?.get("thermometerId")?.values()?.firstOrNull()
        val trucksJson = if (thermometerId != null && thermometerId != TestData.truckAndTowableThermometers.first().id!!.toString()) {
            jacksonObjectMapper().writeValueAsString(emptyArray<Truck>())
        } else {
            jacksonObjectMapper().writeValueAsString(TestData.trucks)
        }

        return Response.Builder.like(response).but().body(trucksJson).build()
    }

    companion object {
        const val NAME = "list-trucks-response-transformer"
    }
}