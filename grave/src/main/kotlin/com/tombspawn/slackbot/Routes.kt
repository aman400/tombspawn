@file:JvmName("SlackRoutes")

package com.tombspawn.slackbot

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tombspawn.ApplicationService
import com.tombspawn.base.common.ErrorResponse
import com.tombspawn.base.common.ListBodyRequest
import com.tombspawn.base.common.SuccessResponse
import com.tombspawn.base.extensions.copyToSuspend
import com.tombspawn.base.extensions.toMap
import com.tombspawn.models.Reference
import com.tombspawn.models.locations.Apps
import com.tombspawn.models.locations.Slack
import com.tombspawn.models.slack.Event
import com.tombspawn.models.slack.SlackEvent
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.*
import io.ktor.response.respond
import io.ktor.routing.Routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.reflect.typeOf

private val LOGGER = LoggerFactory.getLogger("com.tombspawn.slackbot.SlackRoutes")

fun Routing.slackEvent(applicationService: ApplicationService) {
    post<Slack.Event> {
        val slackEvent = call.receive<SlackEvent>()
        LOGGER.debug("SlackEvent: $slackEvent")
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
        LOGGER.debug("SlackAction payload: $payload")
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

        val channelId = params["channel_id"]
        val text = params["text"]
        val responseUrl = params["response_url"]
        val triggerId = params["trigger_id"]

        params.forEach { key, list ->
            LOGGER.info("$key: $list")
        }

        call.respond(HttpStatusCode.OK)
        GlobalScope.launch(Dispatchers.IO) {
            text?.trim()?.toMap()?.let { buildData ->
                applicationService.generateApk(buildData, channelId!!, command.appID, responseUrl!!)
            } ?: run {
                LOGGER.warn("Command options not set. These options can be set using '/build-fleet BRANCH=<git-branch-name>(optional)  BUILD_TYPE=<release/debug>(optional)  FLAVOUR=<flavour>(optional)'")
                applicationService.showGenerateApkDialog(command.appID, triggerId!!)
            }
        }
    }
}


@ExperimentalStdlibApi
fun Routing.apkCallback(applicationService: ApplicationService) {
    post<Apps.App.Callback.Success> { callback ->
        var title = ""
        var receivedFile: File? = null
        val multipart = call.receiveMultipart()
        val otherData = mutableMapOf<String, String>()
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    if (part.name == "title") {
                        title = part.value
                    } else {
                        part.takeIf {
                            part.name != null
                        }?.let {
                            otherData[part.name!!] = part.value
                        }
                    }
                }
                is PartData.FileItem -> {
                    val directory = File(applicationService.uploadDir, callback.callback.callbackId)
                    if(!directory.exists()) {
                        directory.mkdirs()
                    }
                    val file = File(directory, "${part.originalFileName}")
                    part.streamProvider()
                        .use { input -> file.outputStream().buffered().use { output -> input.copyToSuspend(output) } }
                    receivedFile = file
                }
            }

            part.dispose()
            receivedFile?.let {
                applicationService.uploadApk(callback.callback, it, otherData, true)
            }
        }
        applicationService.onTaskCompleted(callback.callback.app.id)
        call.respond("{\"message\": \"ok\"}")
    }

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