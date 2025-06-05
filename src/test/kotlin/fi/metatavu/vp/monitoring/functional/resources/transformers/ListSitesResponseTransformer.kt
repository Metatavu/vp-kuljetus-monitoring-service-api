package fi.metatavu.vp.monitoring.functional.resources.transformers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2
import com.github.tomakehurst.wiremock.http.Response
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import fi.metatavu.vp.deliveryinfo.model.Site
import fi.metatavu.vp.monitoring.functional.resources.TestData

class ListSitesResponseTransformer: ResponseTransformerV2 {
    override fun getName(): String {
        return NAME
    }

    override fun transform(response: Response?, serveEvent: ServeEvent?): Response {
        val queryParams = serveEvent?.request?.queryParams
        val thermometerId = queryParams?.get("thermometerId")?.values()?.firstOrNull()
        val sitesJson = if (thermometerId != null && thermometerId != TestData.terminalThermometers.first().id!!.toString()) {
            jacksonObjectMapper().writeValueAsString(emptyArray<Site>())
        } else {
            jacksonObjectMapper().writeValueAsString(TestData.sites)
        }

        return Response.Builder.like(response).but().body(sitesJson).build()
    }

    companion object {
        const val NAME = "list-sites-response-transformer"
    }
}