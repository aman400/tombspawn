package com.tombspawn.slackbot

import com.google.gson.Gson
import com.tombspawn.base.common.*
import com.tombspawn.data.DatabaseService
import com.tombspawn.data.Ref
import com.tombspawn.models.Reference
import com.tombspawn.models.RequestData
import com.tombspawn.models.config.App
import com.tombspawn.models.config.Slack
import com.tombspawn.models.github.RefType
import com.tombspawn.models.slack.*
import com.tombspawn.utils.Constants
import io.ktor.client.request.forms.formData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.append
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

class SlackService @Inject constructor(private val slackClient: SlackClient, val slack: Slack,
                                       val gson: Gson, private val databaseService: DatabaseService) {
    private val LOGGER = LoggerFactory.getLogger("com.tombspawn.slackbot.SlackService")

    suspend fun sendMessage(message: String, channelId: String, attachments: List<Attachment>?) {
        slackClient.sendMessage(message, channelId, attachments)
    }

    suspend fun sendMessage(url: String, data: RequestData?) {
        return slackClient.sendMessage(url, data)
    }

    suspend fun updateMessage(echoed: String?, channelId: String) {
        return slackClient.updateMessage(gson.fromJson(echoed, SlackMessage::class.java), channelId)
    }

    suspend fun uploadFile(file: File, channelId: String, initialComment: String, onFinish: (() -> Unit)? = null) {
        withContext(Dispatchers.IO) {
            val buf = ByteArray(file.length().toInt())
            FileInputStream(file).use {
                it.read(buf)
            }
            uploadFile(buf, channelId, initialComment, onFinish, file.name)
        }
    }

    suspend fun uploadFile(fileData: ByteArray, channelId: String,
                           initialComment: String, onFinish: (() -> Unit)? = null, fileName: String) {
        withContext(Dispatchers.IO) {
            val formData = formData {
                append("token", slack.botToken)
                append("title", fileName)
                append("filename", fileName)
                append("filetype", "auto")
                append("channels", channelId)
                append("initial_comment", initialComment)
                append(
                    "file",
                    fileData,
                    Headers.build {
                        append(HttpHeaders.ContentType, ContentType.Application.OctetStream)
                        append(HttpHeaders.ContentDisposition, "filename=${fileName}")
                    }
                )
            }

            try {
                when (val response = slackClient.uploadFile(formData)) {
                    is CallSuccess -> {
                        if (response.data?.delivered == true) {
                            LOGGER.info("delivered")
                        } else {
                            sendMessage(
                                "Unable to deliver apk to this channel reason: ${response.data?.error}",
                                channelId,
                                null
                            )
                            LOGGER.error("File Not delivered")
                        }
                    }
                    is CallFailure -> {
                        LOGGER.error("File Not delivered")
                        LOGGER.error(response.errorBody, response.throwable)
                    }
                    is ServerFailure -> {
                        LOGGER.error("Call failed, unable to deliver APK ${response.errorBody}", response.throwable)
                    }
                    is CallError -> {
                        LOGGER.error("File not delivered", response.throwable)
                    }
                }
                onFinish?.invoke()
            } catch (exception: Exception) {
                LOGGER.error("Unable to push apk to Slack.", exception)
                onFinish?.invoke()
            }
        }
    }

    suspend fun sendShowGenerateApkDialog(
        branches: List<Reference>?,
        buildTypes: List<String>?,
        echo: String?,
        triggerId: String,
        callbackId: String,
        app: App
    ) {
        val dialogElementList = mutableListOf<Element>()
        val branchList = mutableListOf<Element.Option>()
        branches?.forEach { branch ->
            branchList.add(Element.Option("${branch.name}(${branch.type.type})", branch.name))
        }
        val defaultValue: String? = if (branches?.size == 1) branchList[0].label else null

        if (branchList.size > 0) {
            dialogElementList.add(
                Element(
                    ElementType.SELECT, "Select Branch",
                    SlackConstants.TYPE_SELECT_BRANCH,
                    defaultValue = defaultValue,
                    options = branchList
                )
            )
        }

        val buildTypeList = mutableListOf<Element.Option>()
        buildTypes?.forEach {
            buildTypeList.add(Element.Option(it, it))
        }

        if (buildTypeList.size > 0) {
            dialogElementList.add(
                Element(
                    ElementType.SELECT, "Select Build Type",
                    SlackConstants.TYPE_SELECT_BUILD_TYPE, options = buildTypeList
                )
            )
        }

        app.elements?.take(2)?.forEach {
            dialogElementList.add(it)
        }

        dialogElementList.add(
            Element(
                ElementType.TEXT_AREA,
                "Advanced Options",
                SlackConstants.TYPE_ADDITIONAL_PARAMS,
                optional = true,
                hint = "Advanced Options for Android Devs",
                maxLength = 3000
            )
        )

        val dialog = dialog {
            this.callbackId = callbackId
            title = "Generate APK"
            submitLabel = "Submit"
            notifyOnCancel = false
            state = echo
            elements {
                +dialogElementList
            }
        }

        withContext(Dispatchers.IO) {
            slackClient.openActionDialog(dialog, slack.botToken, triggerId)
        }
    }

    suspend fun sendShowSubscriptionDialog(
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
                    name = SlackConstants.TYPE_SELECT_BRANCH
                    options {
                        +branchList
                    }
                }
            }
        }
        slackClient.sendShowDialog(dialog, triggerId)
    }

    suspend fun showStandupPopup(triggerId: String) {
        // Handle only single action
        val dialog = dialog {
            callbackId = Constants.Slack.CALLBACK_STANDUP_DIALOG
            title = "Standup notes"
            submitLabel = "Submit"
            notifyOnCancel = false
            elements {
                // Add text are for what person did on last working day
                +element {
                    type = com.tombspawn.models.slack.ElementType.TEXT_AREA
                    label = "What did you do on your last working day?"
                    hint = "For eg: I did nothing yesterday, I regret it today."
                    name = "yesterday"
                    maxLength = 3000
                }
                // Add text are for what person is going to do today
                +element {
                    type = com.tombspawn.models.slack.ElementType.TEXT_AREA
                    label = "What will you do today?"
                    hint =
                        "For eg: Today I will be wasting most of my time by laughing and gossiping around."
                    name = "today"
                    maxLength = 3000
                }
            }
        }
        slackClient.sendShowDialog(dialog, triggerId)
    }


    suspend fun sendShowConfirmGenerateApk(channelId: String, branch: String, callbackId: String) {
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
                                false,
                                mutableMapOf(SlackConstants.TYPE_SELECT_BRANCH to branch)
                            )
                        )
                    }
                }
            }
        )

        sendMessage("New changes are available in `$branch` branch.", channelId, attachments)
    }

    suspend fun subscriptionResponse(
        app: App, callback: GenerateCallback,
        slackEvent: SlackEvent,
        buildTypes: List<String>?
    ) = coroutineScope {
        val updatedMessage = slackEvent.originalMessage?.copy(attachments = null)
        if (callback.generate) {
            var branchList: List<Reference>? = null
            callback.data?.get(SlackConstants.TYPE_SELECT_BRANCH)?.let { branch ->
                branchList = listOf(Reference(branch, RefType.BRANCH))
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
        } else {
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

    @Throws(Exception::class)
    suspend fun fetchBotData(botToken: String): BotInfo.Self? {
        return slackClient.fetchBotData(botToken)
    }

    suspend fun getSlackUsers(token: String, nextCursor: String? = null): List<SlackUser> {
        return slackClient.getSlackUsers(token, nextCursor)
    }

    suspend fun getSlackBotImIds(token: String, nextCursor: String? = null): List<IMListData.IM> {
        return slackClient.getSlackBotImIds(token, nextCursor)
    }

    suspend fun sendSubscribeToBranch(
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
                            RequestData(response = "You are successfully subscribed to `$branch`")
                        )
                    } else {
                        sendMessage(
                            slackEvent.responseUrl,
                            RequestData(response = "You are already subscribed to `$branch`")
                        )
                    }
                }
            }
        }
        slackEvent.dialogResponse?.forEach { map ->
            LOGGER.debug("${map.key}, ${map.value}")
        }
    }

    suspend fun subscribeSlackEvent(slackEvent: SlackEvent) {
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
                                    }?.forEach {
                                        LOGGER.debug(it.name)
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

    suspend fun fetchUser(userId: String) = coroutineScope {
        databaseService.addUser(
            userId,
            null,
            null,
            Constants.Database.USER_TYPE_USER
        )

        slackClient.fetchUser(userId)?.let { user ->
            databaseService.updateUser(
                userId,
                user.name,
                user.email
            )
        }
    }

    suspend fun addUser(userId: String, name: String, email: String, databaseService: DatabaseService) {
        databaseService.addUser(userId, name, email, Constants.Database.USER_TYPE_USER)
    }
}