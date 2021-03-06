@file:JvmName("SlackRoutes")

package com.tombspawn.slackbot

import com.tombspawn.ApplicationService
import com.tombspawn.base.common.SuccessResponse
import com.tombspawn.base.extensions.toMap
import com.tombspawn.models.locations.Apps
import com.tombspawn.models.locations.Slack
import com.tombspawn.models.slack.Event
import com.tombspawn.models.slack.SlackEvent
import com.tombspawn.utils.Constants
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.request.receiveParameters
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger("com.tombspawn.slackbot.SlackRoutes")

fun Routing.slackEvent(applicationService: ApplicationService) {
    post<Slack.Event> {
        val slackEvent = call.receive<SlackEvent>()
        LOGGER.trace("SlackEvent: $slackEvent")
        when (slackEvent.type) {
            Event.EventType.URL_VERIFICATION -> call.respond(slackEvent)
            Event.EventType.RATE_LIMIT -> {
                call.respond(HttpStatusCode.OK)
                LOGGER.error("Slack Api Rate Limit")
            }
            Event.EventType.EVENT_CALLBACK -> {
                call.respond(HttpStatusCode.OK)
                applicationService.subscribeSlackEvent(slackEvent)
            }
            else -> {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

fun Routing.slackAction(
    applicationService: ApplicationService
) {
    post<Slack.Action> {
        val params = call.receive<Parameters>()
        val payload = params["payload"]
        LOGGER.trace("SlackAction payload: $payload")
        GlobalScope.launch(Dispatchers.IO) {
            payload?.let {
                applicationService.handleSlackEvent(it)
            }
        }
        call.respond(HttpStatusCode.OK)
    }
}

fun Routing.buildApp(applicationService: ApplicationService) {
    post<Slack.Command> { command ->
        val params = call.receiveParameters()

        val channelId = params[Constants.Slack.CHANNEL_ID]
        val text = params["text"]
        val triggerId = params[Constants.Slack.TRIGGER_ID]

        params.forEach { key, list ->
            LOGGER.info("$key: $list")
        }

        call.respond(HttpStatusCode.OK)
        GlobalScope.launch(Dispatchers.IO) {
            text?.trim()?.toMap()?.let { buildData ->
                applicationService.generateApk(buildData, channelId!!, command.appID)
            } ?: run {
                LOGGER.warn("Command options not set. These options can be set using '/build-fleet BRANCH=<git-branch-name>(optional)  BUILD_TYPE=<release/debug>(optional)  FLAVOUR=<flavour>(optional)'")
                applicationService.showGenerateApkDialog(command.appID, triggerId!!)
            }
        }
    }
}


@ExperimentalStdlibApi
fun Routing.apkCallback(applicationService: ApplicationService) {
    post<Apps.App.Init> { app ->
        launch(Dispatchers.IO) {
            applicationService.fetchAppData(app.app.id)
        }
        call.respond(HttpStatusCode.OK, SuccessResponse("ok"))
    }

    post<Apps.App.Clean> { app ->
        LOGGER.info(call.receiveText())
        launch(Dispatchers.IO) {
            applicationService.onTaskCompleted(app.app.id)
        }
        call.respond(HttpStatusCode.OK, SuccessResponse("ok"))
    }
}