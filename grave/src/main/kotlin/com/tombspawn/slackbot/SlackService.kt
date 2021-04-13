package com.tombspawn.slackbot

import com.google.gson.Gson
import com.tombspawn.base.common.*
import com.tombspawn.data.DatabaseService
import com.tombspawn.models.Reference
import com.tombspawn.models.RequestData
import com.tombspawn.models.config.App
import com.tombspawn.models.config.Slack
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

class SlackService @Inject constructor(internal val slackClient: SlackClient, val slack: Slack,
                                       internal val gson: Gson, internal val databaseService: DatabaseService) {
    internal val LOGGER = LoggerFactory.getLogger("com.tombspawn.slackbot.SlackService")

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
                        sendMessage(
                            "Unable to upload file",
                            channelId,
                            null
                        )
                        LOGGER.error("File Not delivered")
                        LOGGER.error(response.errorBody, response.throwable)
                    }
                    is ServerFailure -> {
                        sendMessage(
                            "Unable to upload file",
                            channelId,
                            null
                        )
                        LOGGER.error("Call failed, unable to deliver APK ${response.errorBody}", response.throwable)
                    }
                    is CallError -> {
                        sendMessage(
                            "Unable to upload file",
                            channelId,
                            null
                        )
                        LOGGER.error("File not delivered", response.throwable)
                    }
                }
                onFinish?.invoke()
            } catch (exception: Exception) {
                sendMessage(
                    "Unable to upload file",
                    channelId,
                    null
                )
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

    suspend fun updateMessage(updatedMessage: SlackMessage?, channelId: String?) {
        require(updatedMessage != null && channelId != null)
        slackClient.updateMessage(updatedMessage, channelId)
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