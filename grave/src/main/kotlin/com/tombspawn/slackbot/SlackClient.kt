package com.tombspawn.slackbot

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.tombspawn.base.common.*
import com.tombspawn.data.DatabaseService
import com.tombspawn.base.extensions.await
import com.tombspawn.base.extensions.random
import com.tombspawn.base.extensions.toMap
import com.tombspawn.di.qualifiers.SlackHttpClient
import com.tombspawn.docker.DockerApiClient
import com.tombspawn.models.*
import com.tombspawn.models.config.App
import com.tombspawn.models.config.Slack
import com.tombspawn.models.github.RefType
import com.tombspawn.models.slack.*
import com.tombspawn.utils.Constants
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.tombspawn.models.slack.Event
import com.tombspawn.models.slack.IMListData
import com.tombspawn.models.slack.SlackUser
import io.ktor.http.content.PartData
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.net.URL
import javax.inject.Inject
import kotlin.collections.set

class SlackClient @Inject constructor(
    @SlackHttpClient
    private val httpClient: HttpClient,
    private val slack: Slack,
    val gson: Gson
) {
    private val LOGGER = LoggerFactory.getLogger("com.application.slack.client")

    @Throws(Exception::class)
    suspend fun fetchBotData(botToken: String): BotInfo.Self? = coroutineScope {
        val call = httpClient.call {
            method = HttpMethod.Get
            url {
                encodedPath = "/api/rtm.connect"
                parameter("token", botToken)
            }
        }
        return@coroutineScope when (val response = call.await<BotInfo>()) {
            is CallSuccess -> {
                response.data?.let { botInfo ->
                    if (botInfo.ok) {
                        botInfo.self
                    } else {
                        null
                    }
                }
            }
            is CallFailure -> {
                LOGGER.error(response.errorBody, response.throwable)
                null
            }
            is ServerFailure -> {
                LOGGER.error(response.errorBody, response.throwable)
                null
            }
            is CallError -> {
                LOGGER.error("Unable to fetch bot data", response.throwable)
                null
            }
        }
    }

    suspend fun updateMessage(updatedMessage: SlackMessage, channel: String) {
        withContext(Dispatchers.IO) {
            val params = ParametersBuilder().apply {
                append(Constants.Slack.TOKEN, slack.botToken)
                append(Constants.Slack.CHANNEL, channel)
                append(Constants.Slack.TEXT, updatedMessage.message ?: "")
                append(Constants.Slack.TS, updatedMessage.timestamp!!)
                append(Constants.Slack.ATTACHMENTS, gson.toJson(updatedMessage.attachments))
            }.build()
            val call = httpClient.call {
                method = HttpMethod.Post
                url {
                    encodedPath = "/api/chat.update"
                }
                body = FormDataContent(params)
            }
            when (val response = call.await<JsonObject>()) {
                is CallSuccess -> {
                    LOGGER.info("Posted dialog successfully ${response.data}")
                }
                is CallFailure -> {
                    LOGGER.error("Dialog posting failed", response.errorBody)
                }
                is ServerFailure -> {
                    LOGGER.error("Dialog posting failed", response.throwable)
                    null
                }
                is CallError -> {
                    LOGGER.error("Dialog posting failed", response.throwable)
                }
            }.exhaustive
        }
    }

    suspend fun sendMessage(url: String, data: RequestData?) = withContext(Dispatchers.IO) {
        val call = httpClient.call {
            url(url)
            method = HttpMethod.Post
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json)
            data?.let {
                body = it
            }
        }

        when (val response = call.await<JsonObject>()) {
            is CallSuccess -> {
                LOGGER.info(response.data.toString())
            }
            is CallFailure -> {
                LOGGER.error("Unable to send message ${response.errorBody}", response.throwable)
            }
            is ServerFailure -> {
                LOGGER.error("Unable to send message", response.throwable)
            }
            is CallError -> {
                LOGGER.error("Unable to send message", response.throwable)
            }
        }
    }


    suspend fun uploadFile(formData: List<PartData>): Response<CallResponse> {
        val call = httpClient.call {
            url {
                encodedPath = "/api/files.upload"
            }
            method = HttpMethod.Post
            body = MultiPartFormDataContent(formData)
        }
        return call.await()
    }

    suspend fun fetchUser(userId: String): UserProfile? = coroutineScope {
        withContext(Dispatchers.IO) {
            val call = httpClient.call {
                method = HttpMethod.Get
                url {
                    encodedPath = "/api/users.profile.get"
                    parameter("token", slack.authToken)
                    parameter("user", userId)
                }
            }

            when (val response = call.await<SlackProfileResponse>()) {
                is CallSuccess -> {
                    response.data?.user?.let { user ->
                        user
                    }
                }
                is CallFailure -> {
                    LOGGER.error("Unable to fetch user profile", response.throwable)
                    null
                }
                is ServerFailure -> {
                    LOGGER.error("Unable to fetch user profile", response.throwable)
                    null
                }
                is CallError -> {
                    LOGGER.error(response.throwable?.message, response.throwable)
                    null
                }
            }
        }
    }

    suspend fun sendMessage(message: String, channelId: String, attachments: List<Attachment>?) {
        val params = ParametersBuilder().apply {
            append(Constants.Slack.TEXT, message)
            append(Constants.Slack.CHANNEL, channelId)
            append(Constants.Slack.TOKEN, slack.botToken)
            attachments?.let {
                append(Constants.Slack.ATTACHMENTS, gson.toJson(attachments))
            }
        }.build()

        val call = httpClient.call {
            method = HttpMethod.Post
            url {
                encodedPath = "/api/chat.postMessage"
            }
            body = FormDataContent(params)
        }

        when (call.await<JsonObject>()) {
            is CallSuccess -> {
                LOGGER.info("message successfully sent")
            }
            else -> {
                LOGGER.info("message sending failed")
            }
        }
    }

    suspend fun sendMessageEphemeral(
        message: String,
        channelId: String,
        userId: String,
        attachments: List<Attachment>?
    ) {

        val params = ParametersBuilder().apply {
            append(Constants.Slack.ATTACHMENTS, gson.toJson(attachments))
            append(Constants.Slack.TEXT, message)
            append(Constants.Slack.USER, userId)
            append(Constants.Slack.CHANNEL, channelId)
            append(Constants.Slack.TOKEN, slack.botToken)
        }.build()
        val call = httpClient.call {
            method = HttpMethod.Post
            url {
                encodedPath = "/api/chat.postEphemeral"
            }
            body = FormDataContent(params)
        }
        withContext(Dispatchers.IO) {
            when (val response = call.await<JsonObject>()) {
                is CallSuccess -> {
                    LOGGER.info(response.data.toString())
                }
                is CallFailure -> {
                    LOGGER.error(response.errorBody)
                }
                is ServerFailure -> {
                    LOGGER.error("Unable to postEphemeral message", response.throwable)
                }
                is CallError -> {
                    LOGGER.info(response.throwable?.message)
                }
            }.exhaustive
        }
    }

    suspend fun sendShowDialog(dialog: Dialog, triggerId: String) = withContext(Dispatchers.IO) {
        val params = Parameters.build {
            append(Constants.Slack.DIALOG, gson.toJson(dialog))
            append(Constants.Slack.TOKEN, slack.botToken)
            append(Constants.Slack.TRIGGER_ID, triggerId)
        }
        val call = httpClient.call {
            method = HttpMethod.Post
            url {
                encodedPath = "/api/dialog.open"
            }
            this.body = FormDataContent(params)
        }

        when (val response = call.await<JsonObject>()) {
            is CallSuccess -> {
                LOGGER.info(response.data.toString())
            }
            is CallFailure -> {
                LOGGER.error("Unable to open dialog", response.errorBody.toString())
            }
            is ServerFailure -> {
                LOGGER.error("Unable to open dialog", response.throwable)
            }
            is CallError -> {
                LOGGER.error("Unable to open dialog", response.throwable?.message)
            }
        }
    }

    suspend fun openActionDialog(dialog: Dialog, slackBotToken: String, triggerId: String) {
        val params = ParametersBuilder().apply {
            append(Constants.Slack.DIALOG, gson.toJson(dialog))
            append(Constants.Slack.TOKEN, slackBotToken)
            append(Constants.Slack.TRIGGER_ID, triggerId)
        }.build()
        val call = httpClient.call {
            method = HttpMethod.Post
            url {
                encodedPath = "/api/dialog.open"
            }
            body = FormDataContent(params)
        }
        withContext(Dispatchers.IO) {
            when (val response = call.await<JsonObject>()) {
                is CallSuccess -> {
                    LOGGER.info("Posted dialog successfully")
                    LOGGER.info(response.data.toString())
                }
                is CallFailure -> {
                    LOGGER.error("Dialog posting failed", response.throwable)
                }
                is ServerFailure -> {
                    LOGGER.error("Dialog posting failed", response.throwable)
                }
                is CallError -> {
                    LOGGER.error("Dialog posting failed", response.throwable)
                }
            }.exhaustive
        }
    }

    private suspend fun getUserList(token: String, cursor: String? = null, limit: Int? = null): UserResponse? {
        val call = httpClient.call {
            url {
                url(URL("https://slack.com/api/users.list"))
                parameters.append(Constants.Slack.TOKEN, token)
                cursor?.let {
                    parameters.append(Constants.Slack.CURSOR, cursor)
                }
                limit?.let {
                    parameters.append(Constants.Slack.LIMIT, it.toString())
                }
            }
            method = HttpMethod.Get
        }

        return when (val response = call.await<UserResponse>()) {
            is CallSuccess -> {
                response.data?.let {
                    if (it.successful == true) {
                        LOGGER.trace(it.toString())
                        it
                    } else {
                        null
                    }
                }
            }
            is CallFailure -> {
                LOGGER.error(response.throwable?.message, response.throwable)
                null
            }
            is ServerFailure -> {
                LOGGER.error("Unable to fetch user list", response.throwable)
                null
            }
            is CallError -> {
                LOGGER.error(response.throwable?.message, response.throwable)
                null
            }
        }
    }

    private suspend fun getIMList(token: String, cursor: String? = null, limit: Int? = null): IMListData? {
        val call = httpClient.call {
            url {
                encodedPath = "/api/im.list"
                parameters.append(Constants.Slack.TOKEN, token)
                cursor?.let {
                    parameters.append(Constants.Slack.CURSOR, cursor)
                }
                limit?.let {
                    parameters.append(Constants.Slack.LIMIT, it.toString())
                }
            }
            method = HttpMethod.Get
        }
        return when (val response = call.await<IMListData>()) {
            is CallSuccess -> {
                response.data?.let {
                    if (it.ok == true) {
                        LOGGER.trace(it.toString())
                        it
                    } else {
                        null
                    }
                }
            }
            is CallFailure -> {
                LOGGER.error("Unable to fetch im list", response.throwable)
                null
            }
            is ServerFailure -> {
                LOGGER.error("Unable to fetch im list", response.throwable)
                null
            }
            is CallError -> {
                LOGGER.error("Unable to fetch im list", response.throwable)
                null
            }
        }
    }

    suspend fun getSlackUsers(token: String, nextCursor: String?): List<SlackUser> {
        val data = getUserList(token, nextCursor)
        val cursor = data?.responseMetadata?.nextCursor
        val users = mutableListOf<SlackUser>()

        data?.members?.let {
            users.addAll(it)
        }

        if (!cursor.isNullOrEmpty()) {
            users.addAll(getSlackUsers(token, cursor))
        }

        return users
    }

    suspend fun getSlackBotImIds(token: String, nextCursor: String?): List<IMListData.IM> {
        val data = getIMList(token, nextCursor)
        val cursor = data?.responseMetadata?.nextCursor
        val ims = mutableListOf<IMListData.IM>()
        data?.ims?.let {
            ims.addAll(it)
        }
        if (!cursor.isNullOrEmpty()) {
            ims.addAll(getSlackBotImIds(token, cursor))
        }

        return ims
    }
}