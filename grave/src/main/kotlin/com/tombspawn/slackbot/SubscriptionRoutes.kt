package com.tombspawn.slackbot

import com.google.gson.Gson
import com.tombspawn.ApplicationService
import com.tombspawn.data.Database
import com.tombspawn.models.Reference
import com.tombspawn.models.RequestData
import com.tombspawn.models.config.App
import com.tombspawn.models.github.RefType
import com.tombspawn.models.locations.Slack
import com.tombspawn.models.slack.GenerateCallback
import com.tombspawn.models.slack.SlackEvent
import com.tombspawn.models.slack.attachment
import com.tombspawn.models.slack.generateCallback
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
import com.tombspawn.models.slack.Action
import com.tombspawn.models.slack.action
import com.tombspawn.models.slack.confirm

fun Routing.subscribe(applicationService: ApplicationService) {
    post<Slack.Subscribe> { subscription ->
        val parameters = call.receiveParameters()
        val triggerId = parameters[Constants.Slack.TRIGGER_ID]
        applicationService.showSubscriptionDialog(subscription.appID, triggerId!!)
        call.respond(HttpStatusCode.OK)
    }
}

fun CoroutineScope.subscriptionResponse(action: Action, slackClient: SlackClient, slackEvent: SlackEvent,
                                        database: Database, apps: List<App>, gson: Gson) {
    val appId = action.name?.substringAfter(Constants.Slack.CALLBACK_CONFIRM_GENERATE_APK, "")
    apps.firstOrNull {
        it.id == appId
    }?.let {app ->
        val updatedMessage = slackEvent.originalMessage?.copy(attachments = null)
        val callback: GenerateCallback =
            gson.fromJson(action.value, GenerateCallback::class.java)
        if (callback.generate) {
            var branchList: List<Reference>? = null
            callback.data?.get(Constants.Slack.TYPE_SELECT_BRANCH)?.let { branch ->
                branchList = listOf(Reference(branch, RefType.BRANCH))
            }

            updatedMessage?.apply {
                attachments = mutableListOf(
                    attachment {
                        text = ":crossed_fingers: Your APK will be generated soon."
                    }
                )
            }

            launch(Dispatchers.IO) {
                val flavours =
                    database.getFlavours(app.id)?.map { flavour ->
                        flavour.name
                    }

                val buildTypes =
                    database.getBuildTypes(app.id)?.map { buildType ->
                        buildType.name
                    }

                slackClient.sendShowGenerateApkDialog(
                    branchList, buildTypes, flavours, gson.toJson(updatedMessage),
                    slackEvent.triggerId!!,
                    Constants.Slack.CALLBACK_GENERATE_APK + app.id,
                    app.appUrl ?: ""
                )
            }
        } else {
            updatedMessage?.apply {
                attachments {
                    +attachment {
                        text =
                            ":slightly_smiling_face: Thanks for saving the server resources."
                    }
                }
                launch(Dispatchers.IO) {
                    slackClient.updateMessage(updatedMessage, slackEvent.channel?.id!!)
                }
            }
            println("Not generating the APK")
        }
    }
}

suspend fun SlackClient.sendShowConfirmGenerateApk(channelId: String, branch: String, callbackId: String) {
    val attachments = mutableListOf(
        attachment {
            this.callbackId = callbackId
            fallback = "Unable to generate the APK"
            text = "Do you want to generate the APK?"
            id = 1
            color = "#00FF00"
            actions {
                +action {
                    confirm = confirm {
                        text = "This will take up server resources. Generate APK only if you really want it."
                        okText = "Yes"
                        dismissText = "No"
                        title = "Are you sure?"
                    }
                    name = callbackId
                    text = "Yes"
                    type = Action.ActionType.BUTTON
                    style = Action.ActionStyle.PRIMARY
                    value = gson.toJson(
                        generateCallback {
                            generate = true
                            data {
                                +Pair(Constants.Slack.TYPE_SELECT_BRANCH, branch)
                            }
                        }
                    )
                }
                +action {
                    confirm = null
                    name = callbackId
                    text = "No"
                    type = Action.ActionType.BUTTON
                    style = Action.ActionStyle.DEFAULT
                    value = gson.toJson(
                        GenerateCallback(
                            false,
                            mutableMapOf(Constants.Slack.TYPE_SELECT_BRANCH to branch)
                        )
                    )
                }
            }
        }
    )

    sendMessage("New changes are available in `$branch` branch.", channelId, attachments)
}

fun CoroutineScope.sendSubscribeToBranch(slackEvent: SlackEvent, slackClient: SlackClient, database: Database, apps: List<App>) {
    launch(Dispatchers.IO) {
        val appId = slackEvent.callbackId?.substringAfter(Constants.Slack.CALLBACK_SUBSCRIBE_CONSUMER)
        apps.firstOrNull {
            it.id == appId
        }?.let { app ->
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
                                app.id,
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
    }
    slackEvent.dialogResponse?.forEach { map ->
        println("${map.key}, ${map.value}")
    }
}