package com.tombspawn.slackbot

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.tombspawn.base.common.*
import com.tombspawn.data.DatabaseService
import com.tombspawn.base.extensions.await
import com.tombspawn.base.extensions.random
import com.tombspawn.base.extensions.toMap
import com.tombspawn.di.qualifiers.SlackHttpClient
import com.tombspawn.di.qualifiers.UploadDirPath
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
    @UploadDirPath
    private val uploadDirPath: String,
    private val databaseService: DatabaseService,
    private val slack: Slack,
    val gson: Gson
) {
    private val randomWaitingMessages = listOf(
        "Utha le re Baghwan..",
        "Jai Maharashtra!!",
        "Try Holding your Breath!!",
        "Hold your horses!!",
        "Checking Anti-Camp Radius",
        "Creating Randomly Generated Feature",
        "Doing Something You Don't Wanna Know About",
        "Doing The Impossible",
        "Don't Panic",
        "Ensuring Everything Works Perfektly",
        "Generating Plans for Faster-Than-Light Travel",
        "Hitting Your Keyboard Won't Make This Faster",
        "In The Grey, No One Can Hear You Scream",
        "Loading, Don't Wait If You Don't Want To",
        "Preparing to Spin You Around Rapidly"
    )

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
        return@coroutineScope when(val response = call.await<BotInfo>()) {
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
                println(response.errorBody)
                null
            }
            is CallError -> {
                response.throwable?.printStackTrace()
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
                    LOGGER.info("Posted dialog successfully")
                    LOGGER.info(response.data.toString())
                }
                is CallFailure -> {
                    LOGGER.info("Dialog posting failed")
                    LOGGER.error(response.errorBody)
                }
                is CallError -> {
                    LOGGER.info("Dialog posting failed")
                    LOGGER.error("Unable to post dialog", response.throwable)
                }
            }.exhaustive
        }
    }

    suspend fun generateAndUploadApk(
        buildData: MutableMap<String, String>?,
        channelId: String,
        app: App,
        responseUrl: String? = null
    ) {
        val additionalParams = buildData?.get(Constants.Slack.TYPE_ADDITIONAL_PARAMS)?.trim()

        additionalParams?.let {
            it.toMap()?.forEach { key, value ->
                if (!buildData.containsKey(key)) {
                    buildData[key] = value
                }
            }
        }

        withContext(Dispatchers.IO) {
            if (responseUrl != null) {
                sendMessage(responseUrl, RequestData(response = randomWaitingMessages.random()!!))
            } else {
                sendMessage(randomWaitingMessages.random()!!, channelId, null)
            }
        }

        val selectedBranch = buildData?.get(Constants.Slack.TYPE_SELECT_BRANCH)?.trim()

        val userAppPrefix = buildData?.get(Constants.Slack.TYPE_SELECT_APP_PREFIX)?.trim()

        val APKPrefix = "${userAppPrefix?.let {
            "$it-"
        } ?: ""}${System.currentTimeMillis()}"

        withContext(Dispatchers.IO) {
//            val buildVariants = app.gradleExecutor?.fetchBuildVariants()
//            buildVariants?.let {
//                database.addBuildVariants(it, app.id)
//            }

//            val productFlavours = app.gradleExecutor?.fetchProductFlavours()
//            productFlavours?.let {
//                database.addFlavours(it, app.id)
//            }

//            when (val commandResponse = app.gradleExecutor?.generateApp(buildData, uploadDirPath, APKPrefix)) {
//                is Success -> {
//                    val tempDirectory = File(uploadDirPath)
//                    if (tempDirectory.exists()) {
//                        val firstFile = tempDirectory.listFiles()?.firstOrNull { file ->
//                            file?.name?.contains(APKPrefix, true) == true
//                        }
//                        firstFile?.let { file ->
//                            if (file.exists()) {
//                                uploadFile(file, channelId)
//                            } else {
//                                LOGGER.error("APK Generated but file not found in the folder")
//                                LOGGER.error(commandResponse.data)
//                                if (responseUrl != null) {
//                                    sendMessage(
//                                        responseUrl,
//                                        RequestData(
//                                            response = commandResponse.data
//                                                ?: "Something went wrong. Unable to generate the APK"
//                                        )
//                                    )
//                                } else {
//                                    sendMessage(
//                                        commandResponse.data ?: "Something went wrong. Unable to generate the APK",
//                                        channelId,
//                                        null
//                                    )
//                                }
//                            }
//                        } ?: run {
//                            LOGGER.error("APK Generated but not found in the folder")
//                            LOGGER.error(commandResponse.data)
//
//                            if (responseUrl != null) {
//                                sendMessage(
//                                    responseUrl,
//                                    RequestData(
//                                        response = commandResponse.data
//                                            ?: "Something went wrong. Unable to generate the APK"
//                                    )
//                                )
//                            } else {
//                                sendMessage(
//                                    commandResponse.data ?: "Something went wrong. Unable to generate the APK",
//                                    channelId,
//                                    null
//                                )
//                            }
//                        }
//                    } else {
//                        LOGGER.error("APK Generated but not found in the folder")
//                        LOGGER.error(commandResponse.data)
//                        if (responseUrl != null) {
//                            sendMessage(
//                                responseUrl,
//                                RequestData(
//                                    response = commandResponse.data
//                                        ?: "Something went wrong. Unable to generate the APK"
//                                )
//                            )
//                        } else {
//                            sendMessage(
//                                commandResponse.data ?: "Something went wrong. Unable to generate the APK",
//                                channelId,
//                                null
//                            )
//                        }
//                    }
//                }
//
//                is Failure -> {
//                    LOGGER.error(commandResponse.error, commandResponse.throwable)
//                    if (responseUrl != null) {
//                        sendMessage(
//                            responseUrl,
//                            RequestData(
//                                response = commandResponse.error
//                                    ?: "Something went wrong. Unable to generate the APK"
//                            )
//                        )
//                    } else {
//                        sendMessage(
//                            commandResponse.error ?: "Something went wrong. Unable to generate the APK",
//                            channelId,
//                            null
//                        )
//                    }
//                }
//
//            }
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
                LOGGER.error(response.errorBody)
            }
            is CallError -> {
                LOGGER.error("Unable to send message", response.throwable)
            }
        }
    }


    private suspend fun uploadFile(file: File, channelId: String, deleteFile: Boolean = true) {
        withContext(Dispatchers.IO) {
            val buf = ByteArray(file.length().toInt())
            FileInputStream(file).use {
                it.read(buf)
            }
            val formData = formData {
                append("token", slack.botToken)
                append("title", file.nameWithoutExtension)
                append("filename", file.name)
                append("filetype", "auto")
                append("channels", channelId)
                append(
                    "file",
                    buf,
                    Headers.build {
                        append(HttpHeaders.ContentType, ContentType.Application.OctetStream)
                        append(HttpHeaders.ContentDisposition, " filename=${file.name}")
                    }
                )
            }
            val call = httpClient.call {
                url {
                    encodedPath = "/api/files.upload"
                }
                method = HttpMethod.Post
                body = MultiPartFormDataContent(formData)
            }
            try {
                val response = call.await<CallResponse>()
                when (response) {
                    is CallSuccess -> {
                        if (response.data?.delivered == true) {
                            LOGGER.info("delivered")
                        } else {
                            sendMessage(
                                "Unable to deliver apk to this channel reason: ${response.data?.error}",
                                channelId,
                                null
                            )
                            LOGGER.error("Not delivered")
                        }
                    }
                    is CallFailure -> {
                        LOGGER.error("Not delivered")
                        LOGGER.error(response.errorBody, response.throwable)
                    }
                    is CallError -> {
                        LOGGER.error("Call failed unable to deliver APK")
                        LOGGER.error(response.throwable?.message, response.throwable)
                    }
                }.exhaustive

                if (deleteFile)
                    file.delete()
            } catch (exception: Exception) {
                LOGGER.error("Unable to push apk to Slack.", exception)
                if (deleteFile) {
                    file.delete()
                }
            }
        }
    }


    suspend fun fetchUser(userId: String, databaseService: DatabaseService) {
        databaseService.addUser(
            userId,
            null,
            null,
            Constants.Database.USER_TYPE_USER
        )

        fetchAndUpdateUser(userId, databaseService)
    }

    suspend fun addUser(userId: String, name: String, email: String, databaseService: DatabaseService) {
        databaseService.addUser(userId, name, email, Constants.Database.USER_TYPE_USER)
    }

    private suspend fun fetchAndUpdateUser(userId: String, databaseService: DatabaseService) {
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
                        databaseService.updateUser(
                            userId,
                            user.name,
                            user.email
                        )
                    }
                }

                is CallFailure -> {
                    LOGGER.error(response.errorBody, response.throwable)
                }

                is CallError -> {
                    LOGGER.error(response.throwable?.message, response.throwable)
                }
            }
        }
    }


    suspend fun subscribeSlackEvent(databaseService: DatabaseService, slackEvent: SlackEvent) {
        slackEvent.event?.let { event ->
            if (!databaseService.userExists(event.user)) {
                event.user?.let { user ->
                    fetchUser(user, databaseService)
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
                                        println(it.name)
                                    }
                                }
                                Constants.Slack.TYPE_SUBSCRIBE_FLEET -> {
                                    println("Valid Event Fleet")
                                }
                                else -> {
                                    println("Invalid Event")
                                }
                            }
                        }
                    }
                }
                else -> {
                    println("Unknown event type")
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
                LOGGER.info(response.errorBody.toString())
            }
            is CallError -> {
                LOGGER.info(response.throwable?.message)
            }
        }
    }

    suspend fun sendShowCreateApiDialog(verbs: List<String>?, triggerId: String) {
        val verbElements = verbs?.map { verb ->
            Element.Option(verb, verb)
        }

        val dialog = dialog {
            callbackId = Constants.Slack.CALLBACK_CREATE_API
            title = "Create API"
            submitLabel = "Submit"
            notifyOnCancel = false
            elements {
                verbElements?.let { verbs ->
                    +element {
                        type = ElementType.SELECT
                        label = "Select verb"
                        name = Constants.Slack.TYPE_SELECT_VERB
                        options = verbs.toMutableList()
                    }
                }

                +element {
                    type = ElementType.TEXT_AREA
                    label = "Expected Response"
                    name = Constants.Slack.TYPE_SELECT_RESPONSE
                    optional = false
                    hint = "Type your expected response here."
                    maxLength = 3000
                }
            }
        }

        withContext(Dispatchers.IO) {
            openActionDialog(dialog, slack.botToken, triggerId)
        }

    }

    suspend fun sendShowGenerateApkDialog(
        branches: List<Reference>?,
        buildTypes: List<String>?,
        flavours: List<String>?,
        echo: String?,
        triggerId: String,
        callbackId: String,
        defaultAppUrl: String
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
                    Constants.Slack.TYPE_SELECT_BRANCH,
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
                    Constants.Slack.TYPE_SELECT_BUILD_TYPE, options = buildTypeList
                )
            )
        }

        val flavourList = mutableListOf<Element.Option>()
        flavours?.forEach {
            flavourList.add(Element.Option(it, it))
        }

        if (flavourList.size > 0) {
            dialogElementList.add(
                Element(
                    ElementType.SELECT, "Select Flavour",
                    Constants.Slack.TYPE_SELECT_FLAVOUR, options = flavourList
                )
            )
        }

        dialogElementList.add(
            Element(
                ElementType.TEXT,
                "App URL",
                Constants.Slack.TYPE_SELECT_URL,
                defaultAppUrl,
                hint = defaultAppUrl,
                maxLength = 150,
                optional = true,
                defaultValue = defaultAppUrl,
                inputType = Element.InputType.URL
            )
        )

        if (dialogElementList.size < 4) {
            dialogElementList.add(
                Element(
                    ElementType.TEXT,
                    "App Prefix",
                    Constants.Slack.TYPE_SELECT_APP_PREFIX,
                    hint = "Prefixes this along with the generated App name",
                    maxLength = 50,
                    optional = true
                )
            )
        }

        dialogElementList.add(
            Element(
                ElementType.TEXT_AREA,
                "Advanced Options",
                Constants.Slack.TYPE_ADDITIONAL_PARAMS,
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
            openActionDialog(dialog, slack.botToken, triggerId)
        }
    }

    private suspend fun openActionDialog(dialog: Dialog, slackBotToken: String, triggerId: String) {
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
                    LOGGER.info("Dialog posting failed")
                    LOGGER.error(response.errorBody)
                }
                is CallError -> {
                    LOGGER.info("Dialog posting failed")
                    LOGGER.error("Unable to post dialog", response.throwable)
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
                        println(it.toString())
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
                        println(it.toString())
                        it
                    } else {
                        null
                    }
                }
            }
            is CallFailure -> {
                null
            }
            is CallError -> {
                null
            }
        }
    }

    suspend fun getSlackUsers(token: String, slackClient: SlackClient, nextCursor: String?): List<SlackUser> {
        val data = slackClient.getUserList(token, nextCursor)
        val cursor = data?.responseMetadata?.nextCursor
        val users = mutableListOf<SlackUser>()

        data?.members?.let {
            users.addAll(it)
        }

        if (!cursor.isNullOrEmpty()) {
            users.addAll(getSlackUsers(token, slackClient, cursor))
        }

        return users
    }

    suspend fun getSlackBotImIds(token: String, slackClient: SlackClient, nextCursor: String?): List<IMListData.IM> {
        val data = slackClient.getIMList(token, nextCursor)
        val cursor = data?.responseMetadata?.nextCursor
        val ims = mutableListOf<IMListData.IM>()
        data?.ims?.let {
            ims.addAll(it)
        }
        if (!cursor.isNullOrEmpty()) {
            ims.addAll(getSlackBotImIds(token, slackClient, cursor))
        }

        return ims
    }
}