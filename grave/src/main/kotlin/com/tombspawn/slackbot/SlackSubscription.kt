package com.tombspawn.slackbot

import com.tombspawn.base.common.SlackConstants
import com.tombspawn.data.Ref
import com.tombspawn.data.Subscriptions
import com.tombspawn.models.Reference
import com.tombspawn.models.RequestData
import com.tombspawn.models.config.App
import com.tombspawn.models.github.RefType
import com.tombspawn.models.slack.*
import com.tombspawn.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

suspend fun SlackService.subscriptionResponse(
    app: App, callback: GenerateCallback,
    slackEvent: SlackEvent,
    buildTypes: List<String>?
) = withContext(Dispatchers.IO) {
    val updatedMessage = slackEvent.originalMessage?.copy(attachments = null)
    when(callback.action) {
        GenerateCallback.Action.UNSUBSCRIBE -> {
            val userId = slackEvent.user?.id
            val channel = slackEvent.channel?.id
            var branchList: List<Reference>? = null
            callback.data?.get(SlackConstants.TYPE_SELECT_BRANCH)?.let { branch ->
                branchList = listOf(Reference(branch, RefType.BRANCH))
            }
            databaseService.unSubscribeUser(userId!!, app.id, branchList!!.first().name, channel!!)
            updatedMessage?.apply {
                this.message = "You are successfully unsubscribed to `${branchList?.firstOrNull()?.name}` branch for `${app.name}` app."
                slackClient.updateMessage(updatedMessage, channel)
            }
        }
        GenerateCallback.Action.GENERATE -> {
            val dbRefs = databaseService.getRefs(app.id)?.map {
                Reference(it.name, it.type)
            }.orEmpty()
            val dbBranches = dbRefs.map {
                it.name
            }

            var branchList: List<Reference>? = null
            callback.data?.get(SlackConstants.TYPE_SELECT_BRANCH)?.let { branch ->
                branchList = listOf(Reference(branch, RefType.BRANCH)).filter {
                    it.name in dbBranches
                }.ifEmpty {
                    dbRefs
                }
            }

            updatedMessage?.apply {
                attachments = mutableListOf(
                    attachment {
                        text = ":crossed_fingers: Your APK will be generated soon."
                    }
                )
            }

            sendShowGenerateApkDialog(
                branchList, buildTypes,
                gson.toJson(updatedMessage),
                slackEvent.triggerId!!,
                Constants.Slack.CALLBACK_GENERATE_APK + app.id,
                app
            )
        }
        GenerateCallback.Action.DO_NOTHING -> {
            updatedMessage?.apply {
                attachments {
                    +attachment {
                        text =
                            ":slightly_smiling_face: Thanks for saving the server resources."
                    }
                }
                slackClient.updateMessage(updatedMessage, slackEvent.channel?.id!!)
            }
            LOGGER.error("Not generating the APK")
        }
    }
}

suspend fun SlackService.unsubscribeDeletedBranch(app: App, reference: Reference) = withContext(Dispatchers.IO) {
    val callbackId = Constants.Slack.CALLBACK_SUBSCRIBE_BRANCH + app.id
    val subscriptions = databaseService.findSubscriptions(app.id, reference.name)
    val attachments = mutableListOf<Attachment>().apply {
        add(attachment {
            this.callbackId = callbackId
            fallback = "Unable to show subscription popup"
            text = "Do you want to subscribe to different branch?"
            id = 1
            color = "#00FF00"
            actions {
                +action {
                    confirm = confirm {
                        title = "Are you sure?"
                        text = "You want to subscribe to a different branch."
                        dismissText = "No"
                        okText = "Yes"
                    }
                    name = callbackId
                    text = "Yes"
                    type = Action.ActionType.BUTTON
                    style = Action.ActionStyle.PRIMARY
                    value = gson.toJson(
                        callbackMessage<String> {
                            action = CallbackMessage.Action.POSITIVE
                            data = app.id
                        }, CallbackMessage::class.java
                    )
                }
                +action {
                    confirm = confirm {
                        title = "Are you sure?"
                        text = "You do not want to subscribe to a different branch."
                        dismissText = "No"
                        okText = "Yes"
                    }
                    name = callbackId
                    text = "No"
                    type = Action.ActionType.BUTTON
                    style = Action.ActionStyle.DEFAULT
                    value = gson.toJson(
                        callbackMessage<String> {
                            action = CallbackMessage.Action.NEGATIVE
                            data = app.id
                        }, CallbackMessage::class.java
                    )
                }
            }
        })
    }
    subscriptions.orEmpty().forEach { resultRow ->
        sendMessage(
            "`${reference.name}` branch for `${app.name}` app is deleted :scream:. You are unsubscribed from the same.",
            resultRow[Subscriptions.channel], attachments)
    }
}

suspend fun SlackService.sendShowConfirmGenerateApk(app: App, channelId: String, branch: String, callbackId: String) {
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
                            action = GenerateCallback.Action.GENERATE
                            data {
                                +Pair(SlackConstants.TYPE_SELECT_BRANCH, branch)
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
                            GenerateCallback.Action.DO_NOTHING,
                            mutableMapOf(SlackConstants.TYPE_SELECT_BRANCH to branch)
                        )
                    )
                }
                +action {
                    confirm = confirm {
                        title = "Are you sure?"
                        text = "Do you want to unsubscribe from `$branch` branch for `${app.name}` App?"
                        okText = "Yes"
                        dismissText = "No"
                    }
                    name = callbackId
                    text = "Unsubscribe"
                    type = Action.ActionType.BUTTON
                    style = Action.ActionStyle.DANGER
                    value = gson.toJson(
                        generateCallback {
                            action = GenerateCallback.Action.UNSUBSCRIBE
                            data =  mutableMapOf(SlackConstants.TYPE_SELECT_BRANCH to branch)
                        }
                    )
                }
            }
        }
    )

    sendMessage("New changes for `${app.name}` app are available in `$branch` branch.", channelId, attachments)
}

suspend fun SlackService.sendShowSubscriptionDialog(
    refs: List<Pair<App, List<Reference>?>>,
    triggerId: String
) {
    val branchList = mutableListOf<Element.OptionGroups>()
    refs.map { (app, refs) ->
        branchList.add(optionGroups {
            label = app.name ?: app.id
            refs?.forEach { ref ->
                +options {
                    this.label = ref.name
                    this.value = "${app.id}${Constants.Slack.NAME_SEPARATOR}${ref.name}"
                }
            }
        })
    }
    val dialog = dialog {
        callbackId = Constants.Slack.CALLBACK_SUBSCRIBE_CONSUMER
        title = "Subscription Details"
        submitLabel = "Submit"
        notifyOnCancel = false
        elements {
            +element {
                type = ElementType.SELECT
                label = "Select a Branch"
                name = SlackConstants.TYPE_SELECT_BRANCH
                optionsGroup = branchList
            }
        }
    }
    slackClient.sendShowDialog(dialog, triggerId)
}

suspend fun SlackService.sendShowUnSubscriptionDialog(
    refs: List<Pair<App, MutableSet<Reference>>>,
    triggerId: String
) {
    val branchList = mutableListOf<Element.OptionGroups>()
    refs.map { (app, refs) ->
        branchList.add(optionGroups {
            label = app.name ?: app.id
            refs.forEach { ref ->
                +options {
                    this.label = ref.name
                    this.value = "${app.id}${Constants.Slack.NAME_SEPARATOR}${ref.name}"
                }
            }
        })
    }
    val dialog = dialog {
        callbackId = Constants.Slack.CALLBACK_UNSUBSCRIBE_CONSUMER
        title = "Unsubscribe from branch"
        submitLabel = "Unsubscribe"
        notifyOnCancel = false
        elements {
            +element {
                type = ElementType.SELECT
                label = "Select a Branch"
                name = SlackConstants.TYPE_SELECT_BRANCH
                optionsGroup = branchList
            }
        }
    }
    slackClient.sendShowDialog(dialog, triggerId)
}

suspend fun SlackService.sendSubscribeToBranch(
    slackEvent: SlackEvent,
    app: App,
    branch: String,
    channelId: String
) = coroutineScope {
    withContext(Dispatchers.IO) {
        slackEvent.user?.id?.let { userId ->
            if (!databaseService.userExists(userId)) {
                fetchUser(userId)
            }

            if (slackEvent.responseUrl != null) {
                if (databaseService.subscribeUser(
                        userId,
                        app.id,
                        branch,
                        channelId
                    )
                ) {
                    sendMessage(
                        slackEvent.responseUrl,
                        RequestData(response = "You are successfully subscribed to `$branch` branch for `${app.name}` App")
                    )
                } else {
                    sendMessage(
                        slackEvent.responseUrl,
                        RequestData(response = "You are already subscribed to `$branch` branch for `${app.name}` App")
                    )
                }
            }
        }
    }
    slackEvent.dialogResponse?.forEach { map ->
        LOGGER.debug("${map.key}, ${map.value}")
    }
}

suspend fun SlackService.unsubscribeFrom(
    slackEvent: SlackEvent,
    app: App,
    branch: String,
    channelId: String
) = coroutineScope {
    withContext(Dispatchers.IO) {
        val message = "You are successfully unsubscribed from `$branch` branch for `${app.name}` App."
        slackEvent.user?.id?.let { userId ->
            if (!databaseService.userExists(userId)) {
                fetchUser(userId)
            }

            if (slackEvent.responseUrl != null) {
                if (databaseService.unSubscribeUser(
                        userId,
                        app.id,
                        branch,
                        channelId
                    )
                ) {
                    sendMessage(
                        slackEvent.responseUrl,
                        RequestData(response = message)
                    )
                } else {
                    sendMessage(
                        slackEvent.responseUrl,
                        RequestData(response = message)
                    )
                }
            }
        }
    }
    slackEvent.dialogResponse?.forEach { map ->
        LOGGER.debug("${map.key}, ${map.value}")
    }
}

suspend fun SlackService.subscribeSlackEvent(slackEvent: SlackEvent) {
    slackEvent.event?.let { event ->
        if (!databaseService.userExists(event.user)) {
            event.user?.let { user ->
                fetchUser(user)
            }
        }
        when (event.type) {
            Event.EventType.APP_MENTION, Event.EventType.MESSAGE -> {
                withContext(Dispatchers.IO) {
                    val user = databaseService.getUser(Constants.Database.USER_TYPE_BOT)
                    user?.let { bot ->
                        when (event.text?.substringAfter("<@${bot.slackId}>", event.text)?.trim()) {
                            Constants.Slack.TYPE_SUBSCRIBE_CONSUMER -> {
                                databaseService.getRefs(Constants.Common.APP_CONSUMER)?.filter {
                                    it.type == RefType.BRANCH
                                }
                            }
                            Constants.Slack.TYPE_SUBSCRIBE_FLEET -> {
                                LOGGER.debug("Valid Event Fleet")
                            }
                            else -> {
                                LOGGER.error("Invalid Event")
                            }
                        }
                    }
                }
            }
            else -> {
                LOGGER.error("Unknown event type")
            }
        }
    }
}