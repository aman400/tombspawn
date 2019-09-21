package com.ramukaka.slackbot

import com.google.gson.JsonParser
import com.ramukaka.data.Database
import com.ramukaka.models.RequestData
import com.ramukaka.models.locations.ApiMock
import com.ramukaka.models.locations.Slack
import com.ramukaka.models.slack.SlackEvent
import com.ramukaka.utils.Constants
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.locations.*
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

fun Routing.mockApi(database: Database) {
    get<ApiMock.GeneratedApi> {
        database.getApi(it.apiId, Constants.Common.GET)?.let { api ->
            call.respondText(api.response, ContentType.parse("application/json; charset=UTF-8"), HttpStatusCode.OK)
        } ?: call.respond(HttpStatusCode.NotFound, "Api not found")
    }
    put<ApiMock.GeneratedApi> {
        database.getApi(it.apiId, Constants.Common.PUT)?.let { api ->
            call.respondText(api.response, ContentType.parse("application/json; charset=UTF-8"), HttpStatusCode.OK)
        } ?: call.respond(HttpStatusCode.NotFound, "Api not found")
    }
    post<ApiMock.GeneratedApi> {
        database.getApi(it.apiId, Constants.Common.POST)?.let { api ->
            call.response.status(HttpStatusCode.OK)
            call.respondText(api.response, ContentType.parse("application/json; charset=UTF-8"), HttpStatusCode.OK)
        } ?: call.respond(HttpStatusCode.NotFound, "Api not found")
    }
    delete<ApiMock.GeneratedApi> {
        database.getApi(it.apiId, Constants.Common.DELETE)?.let { api ->
            call.respondText(api.response, ContentType.parse("application/json; charset=UTF-8"), HttpStatusCode.OK)
        } ?: call.respond(HttpStatusCode.NotFound, "Api not found")
    }
    patch<ApiMock.GeneratedApi> {
        database.getApi(it.apiId, Constants.Common.PATCH)?.let { api ->
            call.respondText(api.response, ContentType.parse("application/json; charset=UTF-8"), HttpStatusCode.OK)
        } ?: call.respond(HttpStatusCode.NotFound, "Api not found")
    }
    head<ApiMock.GeneratedApi> {
        database.getApi(it.apiId, Constants.Common.HEAD)?.let { api ->
            call.respondText(api.response, ContentType.parse("application/json; charset=UTF-8"), HttpStatusCode.OK)
        } ?: call.respond(HttpStatusCode.NotFound, "Api not found")
    }
    options<ApiMock.GeneratedApi> {
        database.getApi(it.apiId, Constants.Common.OPTIONS)?.let { api ->
            call.respondText(api.response, ContentType.parse("application/json; charset=UTF-8"), HttpStatusCode.OK)
        } ?: call.respond(HttpStatusCode.NotFound, "Api not found")
    }
}

fun Routing.createApi(slackClient: SlackClient, database: Database) {
    post<Slack.MockApi> {
        val params = call.receiveParameters()

        val triggerId = params[Constants.Slack.TRIGGER_ID]

        params.forEach { key, list ->
            LOGGER.info("$key: $list")
        }

        launch(Dispatchers.IO) {
            val verbs = database.getVerbs()
            slackClient.sendShowCreateApiDialog(verbs, triggerId!!)
        }

        call.respond(HttpStatusCode.OK)
    }
}

fun CoroutineScope.createApiDialogResponse(slackClient: SlackClient, slackEvent: SlackEvent, database: Database, baseUrl: String) {
    val verb = slackEvent.dialogResponse?.get(Constants.Slack.TYPE_SELECT_VERB)
    val response = slackEvent.dialogResponse?.get(Constants.Slack.TYPE_SELECT_RESPONSE)

    val id = UUID.randomUUID().toString().replace("-", "", true)

    launch(Dispatchers.IO) {
        try {
            JsonParser().parse(response).asJsonObject
            database.addApi(id, verb!!, response!!)

            slackClient.sendMessage(
                slackEvent.responseUrl!!,
                RequestData(response = "Your `$verb` call is ready with url `${baseUrl}api/mock/$id`")
            )
        } catch (exception: Exception) {
            slackClient.sendMessage(
                slackEvent.responseUrl!!,
                RequestData(response = "Invalid JSON")
            )
        }
    }
}