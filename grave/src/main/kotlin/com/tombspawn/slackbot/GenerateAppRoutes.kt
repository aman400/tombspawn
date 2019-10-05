package com.tombspawn.slackbot

import com.google.gson.Gson
import com.tombspawn.data.Database
import com.tombspawn.data.Ref
import com.tombspawn.base.extensions.toMap
import com.tombspawn.models.Reference
import com.tombspawn.models.config.App
import com.tombspawn.models.locations.Slack
import com.tombspawn.models.slack.*
import com.tombspawn.utils.Constants
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.post
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun Routing.buildApp(apps: List<App>, slackClient: SlackClient, database: Database) {
    post<Slack.Command> { command ->
        apps.firstOrNull {
            it.id == command.appID
        }?.let { app ->
            val params = call.receiveParameters()

            val channelId = params["channel_id"]
            val text = params["text"]
            val responseUrl = params["response_url"]
            val triggerId = params["trigger_id"]

            params.forEach { key, list ->
                LOGGER.info("$key: $list")
            }

            text?.trim()?.toMap()?.let { buildData ->
                slackClient.generateAndUploadApk(buildData, channelId!!, app, responseUrl)
                call.respond(HttpStatusCode.OK)
            } ?: run {
                LOGGER.warn("Command options not set. These options can be set using '/build-fleet BRANCH=<git-branch-name>(optional)  BUILD_TYPE=<release/debug>(optional)  FLAVOUR=<flavour>(optional)'")
                val branchList = database.getRefs(app.id)?.map {
                    Reference(it.name, it.type)
                }
                val flavourList = database.getFlavours(app.id)
                val buildTypesList = database.getBuildTypes(app.id)

                slackClient.sendShowGenerateApkDialog(
                    branchList,
                    buildTypesList?.map { buildType -> buildType.name },
                    flavourList?.map { flavour -> flavour.name },
                    null,
                    triggerId!!,
                    Constants.Slack.CALLBACK_GENERATE_APK + app.id,
                    app.appUrl ?: ""
                )
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

fun CoroutineScope.generateAppDialogResponse(
    slackClient: SlackClient,
    slackEvent: SlackEvent,
    apps: List<App>,
    gson: Gson
) {
    slackEvent.callbackId?.substringAfter(Constants.Slack.CALLBACK_GENERATE_APK, "")?.let { appId ->
        apps.firstOrNull {
            it.id == appId
        }?.let { app ->
            if (!slackEvent.echoed.isNullOrEmpty()) {
                launch(Dispatchers.IO) {
                    slackClient.updateMessage(
                        gson.fromJson(slackEvent.echoed, SlackMessage::class.java),
                        slackEvent.channel?.id!!
                    )
                }
            }
            val buildData = slackEvent.dialogResponse?.filterValues { value ->
                value != null
            }?.mapValues { map -> map.value as String }?.toMutableMap()

            launch(Dispatchers.IO) {
                slackClient.generateAndUploadApk(
                    buildData,
                    slackEvent.channel?.id ?: "general",
                    app,
                    slackEvent.responseUrl
                )
            }
        }
    }
}

suspend fun SlackClient.sendShowSubscriptionDialog(
    branches: List<Ref>?,
    triggerId: String,
    app: App
) {
    val branchList = mutableListOf<Element.Option>()
    branches?.forEach { branch ->
        branchList.add(Element.Option("${branch.name}(${branch.type.type})", branch.name))
    }
    val dialog = dialog {
        callbackId = Constants.Slack.CALLBACK_SUBSCRIBE_CONSUMER + app.id
        title = "Subscription Details"
        submitLabel = "Submit"
        notifyOnCancel = false
        elements {
            +element {
                type = ElementType.SELECT
                label = "Select Branch"
                name = Constants.Slack.TYPE_SELECT_BRANCH
                options {
                    +branchList
                }
            }
        }
    }
    sendShowDialog(dialog, triggerId)
}