package com.tombspawn.slackbot

import com.tombspawn.ApplicationService
import com.tombspawn.models.locations.ApiMock
import com.tombspawn.models.locations.Slack
import com.tombspawn.utils.Constants
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.locations.*
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun Routing.mockApi(applicationService: ApplicationService) {
    get<ApiMock.GeneratedApi> {
        applicationService.getApi(it.apiId, Constants.Common.GET)?.let { api ->
            call.respondText(api.response, ContentType.parse("application/json; charset=UTF-8"), HttpStatusCode.OK)
        } ?: call.respond(HttpStatusCode.NotFound, "Api not found")
    }
    put<ApiMock.GeneratedApi> {
        applicationService.getApi(it.apiId, Constants.Common.PUT)?.let { api ->
            call.respondText(api.response, ContentType.parse("application/json; charset=UTF-8"), HttpStatusCode.OK)
        } ?: call.respond(HttpStatusCode.NotFound, "Api not found")
    }
    post<ApiMock.GeneratedApi> {
        applicationService.getApi(it.apiId, Constants.Common.POST)?.let { api ->
            call.response.status(HttpStatusCode.OK)
            call.respondText(api.response, ContentType.parse("application/json; charset=UTF-8"), HttpStatusCode.OK)
        } ?: call.respond(HttpStatusCode.NotFound, "Api not found")
    }
    delete<ApiMock.GeneratedApi> {
        applicationService.getApi(it.apiId, Constants.Common.DELETE)?.let { api ->
            call.respondText(api.response, ContentType.parse("application/json; charset=UTF-8"), HttpStatusCode.OK)
        } ?: call.respond(HttpStatusCode.NotFound, "Api not found")
    }
    patch<ApiMock.GeneratedApi> {
        applicationService.getApi(it.apiId, Constants.Common.PATCH)?.let { api ->
            call.respondText(api.response, ContentType.parse("application/json; charset=UTF-8"), HttpStatusCode.OK)
        } ?: call.respond(HttpStatusCode.NotFound, "Api not found")
    }
    head<ApiMock.GeneratedApi> {
        applicationService.getApi(it.apiId, Constants.Common.HEAD)?.let { api ->
            call.respondText(api.response, ContentType.parse("application/json; charset=UTF-8"), HttpStatusCode.OK)
        } ?: call.respond(HttpStatusCode.NotFound, "Api not found")
    }
    options<ApiMock.GeneratedApi> {
        applicationService.getApi(it.apiId, Constants.Common.OPTIONS)?.let { api ->
            call.respondText(api.response, ContentType.parse("application/json; charset=UTF-8"), HttpStatusCode.OK)
        } ?: call.respond(HttpStatusCode.NotFound, "Api not found")
    }
}

fun Routing.createApi(applicationService: ApplicationService) {
    post<Slack.MockApi> {
        val params = call.receiveParameters()

        val triggerId = params[Constants.Slack.TRIGGER_ID]

        params.forEach { key, list ->
            LOGGER.info("$key: $list")
        }

        launch(Dispatchers.IO) {
            applicationService.sendShowCreateApiDialog(triggerId!!)
        }

        call.respond(HttpStatusCode.OK)
    }
}