package com.ramukaka.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.ramukaka.data.Database
import com.ramukaka.data.Ref
import com.ramukaka.extensions.await
import com.ramukaka.extensions.random
import com.ramukaka.extensions.toMap
import com.ramukaka.models.*
import com.ramukaka.models.Command
import com.ramukaka.models.Failure
import com.ramukaka.models.Success
import com.ramukaka.models.github.RefType
import com.ramukaka.models.locations.ApiMock
import com.ramukaka.models.locations.Slack
import com.ramukaka.models.slack.*
import com.ramukaka.utils.Constants
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import models.slack.Action
import models.slack.Confirm
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.collections.List
import kotlin.collections.MutableMap
import kotlin.collections.filter
import kotlin.collections.filterValues
import kotlin.collections.firstOrNull
import kotlin.collections.forEach
import kotlin.collections.listOf
import kotlin.collections.map
import kotlin.collections.mapValues
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.collections.toMutableMap


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
        call.respond("")
    }
}

fun Routing.slackEvent(database: Database, slackClient: SlackClient) {
    post<Slack.Event> {
        val slackEvent = call.receive<SlackEvent>()
        println(slackEvent.toString())
        when (slackEvent.type) {
            Constants.Slack.EVENT_TYPE_VERIFICATION -> call.respond(slackEvent)
            Constants.Slack.EVENT_TYPE_RATE_LIMIT -> {
                call.respond("")
                LOGGER.severe("Slack Api Rate Limit")
                println("Api rate limit")
            }
            Constants.Slack.EVENT_TYPE_CALLBACK -> {
                call.respond("")
                slackClient.subscribeSlackEvent(database, slackEvent)
            }
            "interactive_message" -> {

            }
            else -> {
                call.respond("")
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

        call.respond("")
        when (slackEvent.type) {
            Constants.Slack.EVENT_TYPE_INTERACTIVE_MESSAGE -> {
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
            Constants.Slack.EVENT_TYPE_MESSAGE_ACTION -> {
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
            Constants.Slack.EVENT_TYPE_DIALOG -> {
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

        val triggerId = params["trigger_id"]

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

class SlackClient(
    private val httpClient: HttpClient,
    private val slackAuthToken: String,
    private val gradlePath: String, private val uploadDirPath: String,
    private val gradleBotClient: GradleBotClient, private val database: Database,
    private val slackBotToken: String,
    private val requestExecutor: SendChannel<Command>,
    private val responseListeners: MutableMap<String, CompletableDeferred<CommandResponse>>
) {
    private val gson = Gson()
    private val LOGGER = Logger.getLogger("com.application.slack.client")

    init {
        LOGGER.level = Level.ALL
    }

    suspend fun updateMessage(updatedMessage: SlackMessage, channel: String) {
        withContext(Dispatchers.IO) {
            val params = ParametersBuilder().apply {
                append(Constants.Slack.TOKEN, slackBotToken)
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
                is com.ramukaka.network.Success -> {
                    LOGGER.info("Posted dialog successfully")
                    LOGGER.info(response.data.toString())
                }
                is com.ramukaka.network.Failure -> {
                    LOGGER.info("Dialog posting failed")
                    LOGGER.fine(response.errorBody)
                }

                is CallError -> {
                    LOGGER.info("Dialog posting failed")
                    LOGGER.log(Level.SEVERE, "Unable to post dialog", response.throwable)
                }
            }.exhaustive
        }
    }

    suspend fun generateAndUploadApk(
        buildData: MutableMap<String, String>?,
        channelId: String,
        appDir: String,
        responseUrl: String? = null,
        appName: String
    ) {
        val additionalParams = buildData?.get(Constants.Slack.TYPE_ADDITIONAL_PARAMS)?.trim()

        additionalParams?.let {
            it.toMap()?.forEach { key, value ->
                if (!buildData.containsKey(key)) {
                    buildData[key] = value
                }
            }
        }

        buildData?.remove(Constants.Slack.TYPE_ADDITIONAL_PARAMS)

        val userAppPrefix = buildData?.get(Constants.Slack.TYPE_SELECT_APP_PREFIX)?.trim()

        val APKPrefix = "${userAppPrefix?.let {
            "$it-"
        } ?: ""}${System.currentTimeMillis()}"

        buildData?.remove(Constants.Slack.TYPE_SELECT_APP_PREFIX)

        val selectedBranch = buildData?.get(Constants.Slack.TYPE_SELECT_BRANCH)?.trim()
        val pullCodeCommand =
            "$gradlePath pullCode ${selectedBranch?.let { "-P${Constants.Slack.TYPE_SELECT_BRANCH}=$it" } ?: ""}"

        var executableCommand =
            "$gradlePath assembleWithArgs -PFILE_PATH=$uploadDirPath -P${Constants.Slack.TYPE_SELECT_APP_PREFIX}=$APKPrefix"

        buildData?.forEach { key, value ->
            executableCommand += " -P$key=$value"
        }

        withContext(Dispatchers.IO) {
            if (responseUrl != null) {
                sendMessage(responseUrl, RequestData(response = randomWaitingMessages.random()!!))
            } else {
                sendMessage(randomWaitingMessages.random()!!, channelId, null)
            }
        }

        withContext(Dispatchers.IO) {
            val executionDirectory = File(appDir)
            val id = UUID.randomUUID().toString()
            requestExecutor.send(Request(pullCodeCommand, executionDirectory, id = id))
            val pullCodeResponseListener = CompletableDeferred<CommandResponse>()
            responseListeners[id] = pullCodeResponseListener

            when (val pullCodeResponse = pullCodeResponseListener.await()) {
                is Failure -> {
                    LOGGER.log(
                        Level.SEVERE,
                        if (pullCodeResponse.error.isNullOrEmpty()) "Unable to pull code from branch: $selectedBranch" else pullCodeResponse.error,
                        pullCodeResponse.throwable
                    )
                    if (responseUrl != null) {
                        sendMessage(
                            responseUrl,
                            RequestData(
                                response = pullCodeResponse.error
                                    ?: "Unable to pull latest changes from branch `$selectedBranch`."
                            )
                        )
                    } else {
                        sendMessage(
                            pullCodeResponse.error ?: "Unable to pull latest changes from branch `$selectedBranch`.",
                            channelId,
                            null
                        )
                    }
                }
            }

            runBlocking {
                val buildVariants = gradleBotClient.fetchBuildVariants(appDir)
                buildVariants?.let {
                    database.addBuildVariants(it, appName)
                }

                val productFlavours = gradleBotClient.fetchProductFlavours(appDir)
                productFlavours?.let {
                    database.addFlavours(it, appName)
                }
            }


            val buildId = UUID.randomUUID().toString()
            requestExecutor.send(Request(executableCommand, executionDirectory, id = buildId))
            val buildApkResponseListener = CompletableDeferred<CommandResponse>()
            responseListeners[buildId] = buildApkResponseListener

            when (val commandResponse = buildApkResponseListener.await()) {
                is Success -> {
                    val tempDirectory = File(uploadDirPath)
                    if (tempDirectory.exists()) {
                        val firstFile = tempDirectory.listFiles { _, name ->
                            name.contains(APKPrefix, true)
                        }.firstOrNull()
                        firstFile?.let { file ->
                            if (file.exists()) {
                                uploadFile(file, channelId)
                            } else {
                                LOGGER.log(Level.SEVERE, "APK Generated but file not found in the folder")
                                LOGGER.log(Level.SEVERE, commandResponse.data)
                                if (responseUrl != null) {
                                    sendMessage(
                                        responseUrl,
                                        RequestData(
                                            response = commandResponse.data
                                                ?: "Something went wrong. Unable to generate the APK"
                                        )
                                    )
                                } else {
                                    sendMessage(
                                        commandResponse.data ?: "Something went wrong. Unable to generate the APK",
                                        channelId,
                                        null
                                    )
                                }
                            }
                        } ?: run {
                            LOGGER.log(Level.SEVERE, "APK Generated but not found in the folder")
                            LOGGER.log(Level.SEVERE, commandResponse.data)

                            if (responseUrl != null) {
                                sendMessage(
                                    responseUrl,
                                    RequestData(
                                        response = commandResponse.data
                                            ?: "Something went wrong. Unable to generate the APK"
                                    )
                                )
                            } else {
                                sendMessage(
                                    commandResponse.data ?: "Something went wrong. Unable to generate the APK",
                                    channelId,
                                    null
                                )
                            }
                        }
                    } else {
                        LOGGER.log(Level.SEVERE, "APK Generated but not found in the folder")
                        LOGGER.log(Level.SEVERE, commandResponse.data)
                        if (responseUrl != null) {
                            sendMessage(
                                responseUrl,
                                RequestData(
                                    response = commandResponse.data
                                        ?: "Something went wrong. Unable to generate the APK"
                                )
                            )
                        } else {
                            sendMessage(
                                commandResponse.data ?: "Something went wrong. Unable to generate the APK",
                                channelId,
                                null
                            )
                        }
                    }
                }

                is Failure -> {
                    LOGGER.log(Level.SEVERE, commandResponse.error, commandResponse.throwable)
                    if (responseUrl != null) {
                        sendMessage(
                            responseUrl,
                            RequestData(
                                response = commandResponse.error
                                    ?: "Something went wrong. Unable to generate the APK"
                            )
                        )
                    } else {
                        sendMessage(
                            commandResponse.error ?: "Something went wrong. Unable to generate the APK",
                            channelId,
                            null
                        )
                    }
                }

            }
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
            is com.ramukaka.network.Success -> {
                LOGGER.info(response.data.toString())
            }
            is com.ramukaka.network.Failure -> {
                LOGGER.fine(response.errorBody)
            }
            is CallError -> {
                LOGGER.log(Level.SEVERE, "Unable to send message", response.throwable)
            }
        }
    }


    private suspend fun uploadFile(file: File, channelId: String, deleteFile: Boolean = true) {
        val buf = ByteArray( file.length().toInt())
        FileInputStream( file ).use {
            it.read( buf )
        }
        val formData = formData {
            append("token", slackAuthToken)
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

        withContext(Dispatchers.IO) {
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
                    is com.ramukaka.network.Success -> {
                        if (response.data?.delivered == true) {
                            LOGGER.info("delivered")
                        } else {
                            sendMessage(
                                "Unable to deliver apk to this channel reason: ${response.data?.error}",
                                channelId,
                                null
                            )
                            LOGGER.severe("Not delivered")
                        }
                    }
                    is com.ramukaka.network.Failure -> {
                        LOGGER.severe("Not delivered")
                        LOGGER.log(Level.SEVERE, response.errorBody, response.throwable)
                    }
                    is CallError -> {
                        LOGGER.severe("Call failed unable to deliver APK")
                        LOGGER.log(Level.SEVERE, response.throwable?.message, response.throwable)
                    }
                }.exhaustive

                if (deleteFile)
                    file.delete()
            } catch (exception: Exception) {
                LOGGER.log(Level.SEVERE, "Unable to push apk to Slack.", exception)
                if (deleteFile) {
                    file.delete()
                }
            }
        }
    }


    suspend fun fetchUser(userId: String, database: Database) {
        database.addUser(
            userId,
            null,
            null,
            Constants.Database.USER_TYPE_USER
        )

        fetchAndUpdateUser(userId, database)
    }

    private suspend fun fetchAndUpdateUser(userId: String, database: Database) {
        withContext(Dispatchers.IO) {
            val call = httpClient.call {
                method = HttpMethod.Get
                url {
                    encodedPath = "/api/users.profile.get"
                    parameter("token", slackAuthToken)
                    parameter("user", userId)

                }
            }

            when (val response = call.await<SlackProfileResponse>()) {
                is com.ramukaka.network.Success -> {
                    response.data?.user?.let { user ->
                        database.updateUser(
                            userId,
                            user.name,
                            user.email
                        )
                    }
                }

                is com.ramukaka.network.Failure -> {
                    LOGGER.log(Level.SEVERE, response.errorBody, response.throwable)
                }

                is CallError -> {
                    LOGGER.log(Level.SEVERE, response.throwable, null)
                }
            }
        }
    }


    suspend fun subscribeSlackEvent(database: Database, slackEvent: SlackEvent) {
        slackEvent.event?.let { event ->
            if (!database.userExists(event.user)) {
                event.user?.let { user ->
                    fetchUser(user, database)
                }
            }
            when (event.type) {
                Constants.Slack.EVENT_TYPE_APP_MENTION, Constants.Slack.EVENT_TYPE_MESSAGE -> {
                    withContext(Dispatchers.IO) {
                        val user = database.getUser(Constants.Database.USER_TYPE_BOT)
                        user?.let { bot ->
                            when (event.text?.substringAfter("<@${bot.slackId}>", event.text)?.trim()) {
                                Constants.Slack.TYPE_SUBSCRIBE_CONSUMER -> {
                                    database.getRefs(Constants.Common.APP_CONSUMER)?.filter {
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

    private suspend fun sendMessage(message: String, channelId: String, attachments: List<Attachment>?) {
        val params = ParametersBuilder().apply {
            append(Constants.Slack.TEXT, message)
            append(Constants.Slack.CHANNEL, channelId)
            append(Constants.Slack.TOKEN, slackBotToken)
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

        when(call.await<JsonObject>()) {
            is com.ramukaka.network.Success -> {
                LOGGER.info("message successfully sent")
            }
            else -> {
                LOGGER.info("message sending failed")
            }
        }
    }

    private suspend fun sendMessageEphemeral(
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
            append(Constants.Slack.TOKEN, slackBotToken)
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
                is com.ramukaka.network.Success -> {
                    LOGGER.info(response.data.toString())
                }
                is com.ramukaka.network.Failure -> {
                    LOGGER.fine(response.errorBody)
                }
                is CallError -> {
                    LOGGER.info(response.throwable?.message)
                }
            }.exhaustive
        }
    }

    suspend fun sendShowSubscriptionDialog(
        branches: List<Ref>?,
        triggerId: String
    ) {
        val branchList = mutableListOf<Element.Option>()
        branches?.forEach { branch ->
            branchList.add(Element.Option("${branch.name}(${branch.type.type})", branch.name))
        }
        val dialog = Dialog(
            Constants.Slack.CALLBACK_SUBSCRIBE_CONSUMER, "Subscription Details", "Submit", false, null,
            mutableListOf(
                Element(
                    ElementType.SELECT, "Select Branch",
                    Constants.Slack.TYPE_SELECT_BRANCH, options = branchList
                )
            )
        )

        val body = mutableMapOf<String, String?>()
        body[Constants.Slack.DIALOG] = gson.toJson(dialog)
        body[Constants.Slack.TOKEN] = slackBotToken
        body[Constants.Slack.TRIGGER_ID] = triggerId

        withContext(Dispatchers.IO) {
            val call = httpClient.call {
                method = HttpMethod.Get
                url {
                    encodedPath = "/api/dialog.open"
                    parametersOf()
                }
                header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
                this.body = body
            }

            when (val response = call.await<JsonObject>()) {
                is com.ramukaka.network.Success -> {
                    LOGGER.info(response.data.toString())
                }
                is com.ramukaka.network.Failure -> {
                    LOGGER.info(response.errorBody.toString())
                }
                is CallError -> {
                    LOGGER.info(response.throwable?.message)
                }
            }
        }
    }

    suspend fun sendShowConfirmGenerateApk(channelId: String, branch: String) {

        val attachments = mutableListOf(
            Attachment(
                Constants.Slack.CALLBACK_CONFIRM_GENERATE_APK, "Unable to generate the APK",
                "Do you want to generate the APK?", 1, "#00FF00",
                mutableListOf(
                    Action(
                        confirm = Confirm(
                            text = "This will take up server resources. Generate APK only if you really want it.",
                            okText = "Yes",
                            dismissText = "No",
                            title = "Are you sure?"
                        ),
                        name = Constants.Slack.CALLBACK_CONFIRM_GENERATE_APK,
                        text = "Yes",
                        type = Action.ActionType.BUTTON,
                        style = Action.ActionStyle.PRIMARY,
                        value = gson.toJson(
                            GenerateCallback(
                                true,
                                mutableMapOf(Constants.Slack.TYPE_SELECT_BRANCH to branch)
                            )
                        )
                    ),
                    Action(
                        confirm = null,
                        name = Constants.Slack.CALLBACK_CONFIRM_GENERATE_APK,
                        text = "No",
                        type = Action.ActionType.BUTTON,
                        style = Action.ActionStyle.DEFAULT,
                        value = gson.toJson(
                            GenerateCallback(
                                false,
                                mutableMapOf(Constants.Slack.TYPE_SELECT_BRANCH to branch)
                            )
                        )
                    )
                ), Attachment.AttachmentType.DEFAULT
            )
        )

        sendMessage("New changes are available in `$branch` branch.", channelId, attachments)
    }

    suspend fun sendShowCreateApiDialog(verbs: List<String>?, triggerId: String) {
        val dialogElementList = mutableListOf<Element>()

        val verbElements = verbs?.map { verb ->
            Element.Option(verb, verb)
        }

        verbElements?.let {
            dialogElementList.add(
                Element(
                    ElementType.SELECT, "Select verb",
                    Constants.Slack.TYPE_SELECT_VERB,
                    options = it
                )
            )
        }

        dialogElementList.add(
            Element(
                ElementType.TEXT_AREA,
                "Expected Response",
                Constants.Slack.TYPE_SELECT_RESPONSE,
                optional = false,
                hint = "Type your expected response here.",
                maxLength = 3000
            )
        )

        val dialog = Dialog(
            Constants.Slack.CALLBACK_CREATE_API, "Create API", "Submit", false,
            elements = dialogElementList
        )

        withContext(Dispatchers.IO) {
            openActionDialog(dialog, slackBotToken, triggerId)
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

        val dialog = Dialog(
            callbackId, "Generate APK", "Submit", false, echo,
            dialogElementList
        )

        withContext(Dispatchers.IO) {
            openActionDialog(dialog, slackBotToken, triggerId)
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
                is com.ramukaka.network.Success -> {
                    LOGGER.info("Posted dialog successfully")
                    LOGGER.info(response.data.toString())
                }
                is com.ramukaka.network.Failure -> {
                    LOGGER.info("Dialog posting failed")
                    LOGGER.fine(response.errorBody)
                }
                is CallError -> {
                    LOGGER.info("Dialog posting failed")
                    LOGGER.log(Level.SEVERE, "Unable to post dialog", response.throwable)
                }
            }.exhaustive
        }
    }
}