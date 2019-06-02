package com.ramukaka.slackbot

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.ramukaka.data.Database
import com.ramukaka.extensions.toMap
import com.ramukaka.models.Reference
import com.ramukaka.models.RequestData
import com.ramukaka.models.github.RefType
import com.ramukaka.models.locations.ApiMock
import com.ramukaka.models.locations.Slack
import com.ramukaka.models.slack.*
import com.ramukaka.utils.Constants
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import models.slack.Action
import models.slack.Event
import java.util.*
import java.util.logging.Logger

val LOGGER = Logger.getLogger("com.application.slack.routing")

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


fun Routing.subscribe() {
    post<Slack.Subscribe> {
        call.respond(HttpStatusCode.OK)
    }
}

fun Routing.slackEvent(database: Database, slackClient: SlackClient) {
    post<Slack.Event> {
        val slackEvent = call.receive<SlackEvent>()
        println(slackEvent.toString())
        when (slackEvent.type) {
            Event.EventType.URL_VERIFICATION -> call.respond(slackEvent)
            Event.EventType.RATE_LIMIT -> {
                call.respond(HttpStatusCode.OK)
                LOGGER.severe("Slack Api Rate Limit")
                println("Api rate limit")
            }
            Event.EventType.EVENT_CALLBACK -> {
                call.respond(HttpStatusCode.OK)
                slackClient.subscribeSlackEvent(database, slackEvent)
            }
            Event.EventType.INTERACTIVE_MESSAGE -> {

            }
            else -> {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

fun Routing.slackAction(
    database: Database,
    slackClient: SlackClient,
    consumerAppDir: String,
    baseUrl: String,
    fleetAppDir: String,
    consumerAppUrl: String,
    fleetAppUrl: String
) {
    post<Slack.Action> {
        val params = call.receive<Parameters>()
        val payload = params["payload"]
        println(payload)
        val slackEvent = Gson().fromJson<SlackEvent>(payload, SlackEvent::class.java)
        println(slackEvent.toString())

        call.respond(HttpStatusCode.OK)
        when (slackEvent.type) {
            Event.EventType.INTERACTIVE_MESSAGE -> {
                when(slackEvent.callbackId) {
                    // For standup bot
                    Constants.Slack.CALLBACK_STANDUP_MESSAGE -> {
                        // Handle only single action
                        val dialog = dialog {
                            callbackId = Constants.Slack.CALLBACK_STANDUP_DIALOG
                            title = "Standup notes"
                            submitLabel = "Submit"
                            notifyOnCancel = false
                            elements {
                                +element {
                                    type = ElementType.TEXT_AREA
                                    label = "What did you do on your last working day?"
                                    hint = "For eg: I did nothing yesterday, I regret it today."
                                    name = "yesterday"
                                    maxLength = 3000
                                }
                                +element {
                                    type = ElementType.TEXT_AREA
                                    label = "What will you do today?"
                                    hint = "For eg: Today I will be wasting most of my time by laughing and gossiping around."
                                    name = "today"
                                    maxLength = 3000
                                }
                            }
                        }
                        slackClient.sendShowDialog(dialog, slackEvent.triggerId!!)
                    }
                    else -> {
                        slackEvent.actions?.forEach { action ->
                            when (action.name) {
                                // User confirmed APK Generation from dialog box
                                Constants.Slack.CALLBACK_CONFIRM_GENERATE_APK -> {
                                    val updatedMessage = slackEvent.originalMessage?.copy(attachments = null)
                                    val callback: GenerateCallback = Gson().fromJson(action.value, GenerateCallback::class.java)
                                    if (callback.generate) {
                                        var branchList: List<Reference>? = null
                                        callback.data?.get(Constants.Slack.TYPE_SELECT_BRANCH)?.let { branch ->
                                            branchList = listOf(Reference(branch, RefType.BRANCH))
                                        }
                                        updatedMessage?.apply {
                                            attachments = mutableListOf(
                                                Attachment(text = ":crossed_fingers: Your APK will be generated soon.")
                                            )
                                        }

                                        val flavours = database.getFlavours(Constants.Common.APP_CONSUMER)?.map { flavour ->
                                            flavour.name
                                        }

                                        val buildTypes =
                                            database.getBuildTypes(Constants.Common.APP_CONSUMER)?.map { buildType ->
                                                buildType.name
                                            }

                                        slackClient.sendShowGenerateApkDialog(
                                            branchList, buildTypes, flavours, Gson().toJson(updatedMessage),
                                            slackEvent.triggerId!!,
                                            Constants.Slack.CALLBACK_GENERATE_CONSUMER_APK,
                                            consumerAppUrl
                                        )
                                    } else {
                                        updatedMessage?.apply {
                                            attachments = mutableListOf(
                                                Attachment(text = ":slightly_smiling_face: Thanks for saving the server resources.")
                                            )
                                            launch(Dispatchers.IO) {
                                                slackClient.updateMessage(updatedMessage, slackEvent.channel?.id!!)
                                            }
                                        }
                                        println("Not generating the APK")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Event.EventType.MESSAGE_ACTION -> {
                when (slackEvent.callbackId) {
                    Constants.Slack.TYPE_SUBSCRIBE_CONSUMER ->
                        slackClient.sendShowSubscriptionDialog(
                            database.getRefs(Constants.Common.APP_CONSUMER),
                            slackEvent.triggerId!!
                        )
                    Constants.Slack.TYPE_GENERATE_CONSUMER -> {
                        val branchList = database.getRefs(Constants.Common.APP_CONSUMER)?.map {
                            Reference(it.name, it.type)
                        }
                        val flavourList = database.getFlavours(Constants.Common.APP_CONSUMER)
                        val buildTypesList = database.getBuildTypes(Constants.Common.APP_CONSUMER)

                        slackClient.sendShowGenerateApkDialog(
                            branchList,
                            buildTypesList?.map { buildType -> buildType.name },
                            flavourList?.map { flavour -> flavour.name },
                            null,
                            slackEvent.triggerId!!,
                            Constants.Slack.CALLBACK_GENERATE_CONSUMER_APK,
                            consumerAppUrl
                        )
                    }

                    Constants.Slack.TYPE_GENERATE_FLEET -> {
                        val branchList = database.getRefs(Constants.Common.APP_FLEET)?.map {
                            Reference(it.name, it.type)
                        }
                        val flavourList = database.getFlavours(Constants.Common.APP_FLEET)
                        val buildTypesList = database.getBuildTypes(Constants.Common.APP_FLEET)

                        slackClient.sendShowGenerateApkDialog(
                            branchList,
                            buildTypesList?.map { buildType -> buildType.name },
                            flavourList?.map { flavour -> flavour.name },
                            null,
                            slackEvent.triggerId!!,
                            Constants.Slack.CALLBACK_GENERATE_FLEET_APK,
                            fleetAppUrl
                        )
                    }

                    Constants.Slack.TYPE_CREATE_MOCK_API -> {
                        slackClient.sendShowCreateApiDialog(database.getVerbs(), slackEvent.triggerId!!)
                    }
                }
            }
            Event.EventType.DIALOG_SUBMISSION -> {
                when (slackEvent.callbackId) {
                    Constants.Slack.CALLBACK_SUBSCRIBE_CONSUMER -> {
                        launch(Dispatchers.IO) {
                            slackEvent.user?.id?.let { userId ->
                                if (!database.userExists(userId)) {
                                    slackClient.fetchUser(userId, database)
                                }
                                val branch = slackEvent.dialogResponse?.get(Constants.Slack.TYPE_SELECT_BRANCH)
                                val channelId = slackEvent.channel?.id
                                if (branch != null) {
                                    if (slackEvent.responseUrl != null) {
                                        if (database.subscribeUser(
                                                userId,
                                                Constants.Common.APP_CONSUMER,
                                                branch,
                                                channelId!!
                                            )
                                        ) {
                                            slackClient.sendMessage(
                                                slackEvent.responseUrl,
                                                RequestData(response = "You are successfully subscribed to `$branch`")
                                            )
                                        } else {
                                            slackClient.sendMessage(
                                                slackEvent.responseUrl,
                                                RequestData(response = "You are already subscribed to `$branch`")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        slackEvent.dialogResponse?.forEach { map ->
                            println("${map.key}, ${map.value}")
                        }
                    }

                    Constants.Slack.CALLBACK_GENERATE_CONSUMER_APK -> {
                        if (!slackEvent.echoed.isNullOrEmpty()) {
                            launch(Dispatchers.IO) {
                                slackClient.updateMessage(
                                    Gson().fromJson(slackEvent.echoed, SlackMessage::class.java),
                                    slackEvent.channel?.id!!
                                )
                            }
                        }
                        val buildData = slackEvent.dialogResponse?.filterValues { value ->
                            value != null
                        }?.mapValues { map -> map.value as String }?.toMutableMap()

                        slackClient.generateAndUploadApk(
                            buildData,
                            slackEvent.channel?.id ?: "general",
                            consumerAppDir,
                            slackEvent.responseUrl,
                            Constants.Common.APP_CONSUMER
                        )
                    }

                    Constants.Slack.CALLBACK_GENERATE_FLEET_APK -> {
                        val buildData = slackEvent.dialogResponse?.filterValues { value ->
                            value != null
                        }?.mapValues { map -> map.value as String }?.toMutableMap()

                        slackClient.generateAndUploadApk(
                            buildData,
                            slackEvent.channel?.id ?: "general",
                            fleetAppDir,
                            slackEvent.responseUrl,
                            Constants.Common.APP_FLEET
                        )
                    }

                    Constants.Slack.CALLBACK_CREATE_API -> {
                        val verb = slackEvent.dialogResponse?.get(Constants.Slack.TYPE_SELECT_VERB)
                        val response = slackEvent.dialogResponse?.get(Constants.Slack.TYPE_SELECT_RESPONSE)

                        val id = UUID.randomUUID().toString().replace("-", "", true)

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
            }
        }
    }
}

fun Routing.buildConsumer(appDir: String, slackClient: SlackClient, database: Database, defaultAppUrl: String) {
    post<Slack.Consumer> {
        val params = call.receiveParameters()

        val channelId = params["channel_id"]
        val text = params["text"]
        val responseUrl = params["response_url"]
        val triggerId = params["trigger_id"]

        params.forEach { key, list ->
            LOGGER.info("$key: $list")
        }

        text?.trim()?.toMap()?.let { buildData ->
            slackClient.generateAndUploadApk(buildData, channelId!!, appDir, responseUrl, Constants.Common.APP_CONSUMER)
            call.respond(HttpStatusCode.OK)
        } ?: run {
            LOGGER.warning("Command options not set. These options can set using '/build-consumer BRANCH=<git-branch-name>(optional)  BUILD_TYPE=<release/debug>(optional)  FLAVOUR=<flavour>(optional)'")
            val branchList = database.getRefs(Constants.Common.APP_CONSUMER)?.map {
                Reference(it.name, it.type)
            }
            val flavourList = database.getFlavours(Constants.Common.APP_CONSUMER)
            val buildTypesList = database.getBuildTypes(Constants.Common.APP_CONSUMER)

            slackClient.sendShowGenerateApkDialog(
                branchList,
                buildTypesList?.map { buildType -> buildType.name },
                flavourList?.map { flavour -> flavour.name },
                null,
                triggerId!!,
                Constants.Slack.CALLBACK_GENERATE_CONSUMER_APK,
                defaultAppUrl
            )
            call.respond(HttpStatusCode.OK)
        }
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

fun Routing.standup(slackClient: SlackClient) {
    post<Slack.Standup> {
        val params = call.receiveParameters()

        val channel = params[Constants.Slack.CHANNEL_ID]
        params.forEach { key, list ->
            LOGGER.info("$key: $list")
        }

        launch(Dispatchers.IO) {
            val attachment = Attachment(
                Constants.Slack.CALLBACK_STANDUP_MESSAGE,
                "Please post your standup updates", "Please post your standup updates.", 1, "#0000FF",
                mutableListOf(
                    Action(null, Constants.Slack.CALLBACK_STANDUP_DIALOG, "Update", Action.ActionType.BUTTON, style = Action.ActionStyle.PRIMARY)
                )
            )
            slackClient.sendMessage("Please update your standup notes", channel!!, listOf(attachment))
        }
        call.respond(HttpStatusCode.OK)
    }
}

fun Routing.buildFleet(appDir: String, slackClient: SlackClient, database: Database, defaultAppUrl: String) {
    post<Slack.Fleet> {
        val params = call.receiveParameters()

        val channelId = params["channel_id"]
        val text = params["text"]
        val responseUrl = params["response_url"]
        val triggerId = params["trigger_id"]

        params.forEach { key, list ->
            LOGGER.info("$key: $list")
        }

        text?.trim()?.toMap()?.let { buildData ->
            slackClient.generateAndUploadApk(buildData, channelId!!, appDir, responseUrl, Constants.Common.APP_FLEET)
            call.respond(HttpStatusCode.OK)
        } ?: run {
            LOGGER.warning("Command options not set. These options can be set using '/build-fleet BRANCH=<git-branch-name>(optional)  BUILD_TYPE=<release/debug>(optional)  FLAVOUR=<flavour>(optional)'")
            val branchList = database.getRefs(Constants.Common.APP_FLEET)?.map {
                Reference(it.name, it.type)
            }
            val flavourList = database.getFlavours(Constants.Common.APP_FLEET)
            val buildTypesList = database.getBuildTypes(Constants.Common.APP_FLEET)

            slackClient.sendShowGenerateApkDialog(
                branchList,
                buildTypesList?.map { buildType -> buildType.name },
                flavourList?.map { flavour -> flavour.name },
                null,
                triggerId!!,
                Constants.Slack.CALLBACK_GENERATE_FLEET_APK,
                defaultAppUrl
            )
            call.respond(HttpStatusCode.OK)
        }
    }
}