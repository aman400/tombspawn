package com.ramukaka.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.ramukaka.data.Branch
import com.ramukaka.data.Database
import com.ramukaka.extensions.await
import com.ramukaka.extensions.random
import com.ramukaka.extensions.toMap
import com.ramukaka.models.*
import com.ramukaka.models.Command
import com.ramukaka.models.Failure
import com.ramukaka.models.Success
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
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import models.slack.Action
import models.slack.BotInfo
import models.slack.Confirm
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import java.io.File
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger


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
                launch {
                    slackClient.subscribeSlackEvent(database, slackEvent)
                }
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
                                var branchList: List<String>? = null
                                callback.data?.get(Constants.Slack.TYPE_SELECT_BRANCH)?.let { branch ->
                                    branchList = listOf(branch)
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
                            database.getBranches(Constants.Common.APP_CONSUMER),
                            slackEvent.triggerId!!
                        )
                    Constants.Slack.TYPE_GENERATE_CONSUMER -> {
                        launch {
                            val branchList = database.getBranches(Constants.Common.APP_CONSUMER)
                            val flavourList = database.getFlavours(Constants.Common.APP_CONSUMER)
                            val buildTypesList = database.getBuildTypes(Constants.Common.APP_CONSUMER)

                            slackClient.sendShowGenerateApkDialog(
                                branchList?.map { branch -> branch.branchName },
                                buildTypesList?.map { buildType -> buildType.name },
                                flavourList?.map { flavour -> flavour.name },
                                null,
                                slackEvent.triggerId!!,
                                Constants.Slack.CALLBACK_GENERATE_CONSUMER_APK,
                                consumerAppUrl
                            )
                        }
                    }

                    Constants.Slack.TYPE_GENERATE_FLEET -> {
                        launch {
                            val branchList = database.getBranches(Constants.Common.APP_FLEET)
                            val flavourList = database.getFlavours(Constants.Common.APP_FLEET)
                            val buildTypesList = database.getBuildTypes(Constants.Common.APP_FLEET)

                            slackClient.sendShowGenerateApkDialog(
                                branchList?.map { branch -> branch.branchName },
                                buildTypesList?.map { buildType -> buildType.name },
                                flavourList?.map { flavour -> flavour.name },
                                null,
                                slackEvent.triggerId!!,
                                Constants.Slack.CALLBACK_GENERATE_FLEET_APK,
                                fleetAppUrl
                            )
                        }
                    }

                    Constants.Slack.TYPE_CREATE_MOCK_API -> {
                        launch {
                            slackClient.sendShowCreateApiDialog(database.getVerbs(), slackEvent.triggerId!!)
                        }
                    }
                }
            }
            Constants.Slack.EVENT_TYPE_DIALOG -> {
                when (slackEvent.callbackId) {
                    Constants.Slack.CALLBACK_SUBSCRIBE_CONSUMER -> {
                        launch {
                            slackEvent.user?.id?.let { userId ->
                                if (!database.userExists(userId)) {
                                    runBlocking {
                                        slackClient.fetchUser(userId, database)
                                    }
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
                                            launch(Dispatchers.IO) {
                                                slackClient.sendMessage(
                                                    slackEvent.responseUrl,
                                                    RequestData(response = "You are successfully subscribed to `$branch`")
                                                )
                                            }
                                        } else {
                                            launch(Dispatchers.IO) {
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
        } ?: launch {
            LOGGER.warning("Command options not set. These options can set using '/build-consumer BRANCH=<git-branch-name>(optional)  BUILD_TYPE=<release/debug>(optional)  FLAVOUR=<flavour>(optional)'")
            val branchList = database.getBranches(Constants.Common.APP_CONSUMER)
            val flavourList = database.getFlavours(Constants.Common.APP_CONSUMER)
            val buildTypesList = database.getBuildTypes(Constants.Common.APP_CONSUMER)

            slackClient.sendShowGenerateApkDialog(
                branchList?.map { branch -> branch.branchName },
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

        launch {
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
        } ?: launch {
            LOGGER.warning("Command options not set. These options can be set using '/build-fleet BRANCH=<git-branch-name>(optional)  BUILD_TYPE=<release/debug>(optional)  FLAVOUR=<flavour>(optional)'")
            val branchList = database.getBranches(Constants.Common.APP_FLEET)
            val flavourList = database.getFlavours(Constants.Common.APP_FLEET)
            val buildTypesList = database.getBuildTypes(Constants.Common.APP_FLEET)

            slackClient.sendShowGenerateApkDialog(
                branchList?.map { branch -> branch.branchName },
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

    interface SlackApi {
        companion object {
            const val BASE_URL = "https://slack.com"
            const val PARAM_TOKEN = "token"
            const val PARAM_USER_ID = "user"
        }

        @GET("/api/users.profile.get")
        fun getProfile(@QueryMap queryMap: MutableMap<String, String>): Call<SlackProfileResponse>

        @FormUrlEncoded
        @POST("/api/chat.postMessage")
        fun postAction(@HeaderMap headers: MutableMap<String, String>, @FieldMap body: MutableMap<String, String?>): Call<JsonObject>

        @FormUrlEncoded
        @POST("/api/dialog.open")
        fun sendActionOpenDialog(@HeaderMap headers: MutableMap<String, String>, @FieldMap body: MutableMap<String, String?>): Call<JsonObject>

        @FormUrlEncoded
        @POST("/api/chat.postEphemeral")
        fun sendMessageEphemeral(@HeaderMap headers: MutableMap<String, String>, @FieldMap body: MutableMap<String, String?>): Call<JsonObject>

        @GET("/api/channels.info")
        fun getChannelInfo(
            @HeaderMap headers: MutableMap<String, String>, @Query(Constants.Slack.TOKEN) token: String,
            @Query(Constants.Slack.CHANNEL) channelId: String
        ): Call<ChannelInfoResponse>

        @FormUrlEncoded
        @POST("/api/chat.update")
        fun updateMessage(
            @Field(Constants.Slack.TOKEN) token: String,
            @Field(Constants.Slack.CHANNEL) channel: String,
            @Field(Constants.Slack.TEXT) text: String,
            @Field(Constants.Slack.TS) timestamp: String,
            @Field(Constants.Slack.ATTACHMENTS) attachments: String?
        ): Call<JsonObject>

        @Multipart
        @POST("/api/files.upload")
        fun pushApp(
            @Part("token") token: RequestBody,
            @Part("title") title: RequestBody,
            @Part("filename") filename: RequestBody,
            @Part("filetype") filetype: RequestBody,
            @Part("channels") channels: RequestBody,
            @Part body: MultipartBody.Part
        ): Call<com.ramukaka.models.Response>

        @POST
        fun sendMessage(
            @HeaderMap header: MutableMap<String, String>, @Url url: String,
            @Body requestBody: RequestData?
        ): Call<ResponseBody>

        @GET("/api/rtm.connect")
        fun fetchBotInfo(
            @HeaderMap headers: MutableMap<String, String>,
            @Query("token") botToken: String
        ): Call<BotInfo>
    }

    suspend fun updateMessage(updatedMessage: SlackMessage, channel: String) {
        val api = ServiceGenerator.createService(SlackApi::class.java, isLoggingEnabled = true)

        withContext(Dispatchers.IO) {
            when (val response = api.updateMessage(
                slackBotToken, channel, updatedMessage.message ?: "",
                updatedMessage.timestamp!!, gson.toJson(updatedMessage.attachments)
            ).await()) {
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

    suspend fun sendMessage(url: String, data: RequestData?) {
        val headers = mutableMapOf("Content-type" to "application/json")
        val api = ServiceGenerator.createService(SlackApi::class.java, isLoggingEnabled = true)
        withContext(Dispatchers.IO) {
            val response = api.sendMessage(headers, url, data).await()
            when (response) {
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
    }


    suspend fun uploadFile(file: File, channelId: String, deleteFile: Boolean = true) {
        val requestBody =
            RequestBody.create(MediaType.parse(ServiceGenerator.MULTIPART_FORM_DATA), file)
        val multipartBody =
            MultipartBody.Part.createFormData("file", "App-debug.apk", requestBody)

        val appToken = RequestBody.create(
            okhttp3.MultipartBody.FORM,
            slackBotToken
        )
        val title = RequestBody.create(okhttp3.MultipartBody.FORM, file.nameWithoutExtension)
        val filename = RequestBody.create(okhttp3.MultipartBody.FORM, file.name)
        val fileType = RequestBody.create(okhttp3.MultipartBody.FORM, "auto")
        val channels = RequestBody.create(okhttp3.MultipartBody.FORM, channelId)

        val api = ServiceGenerator.createService(SlackApi::class.java, SlackApi.BASE_URL, false)
        val call = api.pushApp(appToken, title, filename, fileType, channels, multipartBody)
        withContext(Dispatchers.IO) {
            try {
                val response = call.await()
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
        runBlocking {
            database.addUser(
                userId,
                null,
                null,
                Constants.Database.USER_TYPE_USER
            )
        }

        fetchAndUpdateUser(userId, database)
    }

    private suspend fun fetchAndUpdateUser(userId: String, database: Database) {
        val api = ServiceGenerator.createService(
            SlackApi::class.java, SlackApi.BASE_URL,
            true
        )
        val queryParams = mutableMapOf<String, String>()
        queryParams[SlackApi.PARAM_TOKEN] = slackAuthToken
        queryParams[SlackApi.PARAM_USER_ID] = userId
        withContext(Dispatchers.IO) {
            when (val response = api.getProfile(queryParams).await()) {
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
            }.exhaustive
        }
    }


    suspend fun subscribeSlackEvent(database: Database, slackEvent: SlackEvent) {
        slackEvent.event?.let { event ->
            if (!database.userExists(event.user)) {
                event.user?.let { user ->
                    runBlocking {
                        fetchUser(user, database)
                    }
                }
            }
            when (event.type) {
                Constants.Slack.EVENT_TYPE_APP_MENTION, Constants.Slack.EVENT_TYPE_MESSAGE -> {
                    withContext(Dispatchers.IO) {
                        val user = database.getUser(Constants.Database.USER_TYPE_BOT)
                        user?.let { bot ->
                            when (event.text?.substringAfter("<@${bot.slackId}>", event.text)?.trim()) {
                                Constants.Slack.TYPE_SUBSCRIBE_CONSUMER -> {
                                    database.getBranches(Constants.Common.APP_CONSUMER)?.forEach {
                                        println(it.branchName)
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
        val body = mutableMapOf<String, String?>()
        attachments?.let {
            body[Constants.Slack.ATTACHMENTS] = gson.toJson(attachments)
        }
        body[Constants.Slack.TEXT] = message
        body[Constants.Slack.CHANNEL] = channelId
        body[Constants.Slack.TOKEN] = slackBotToken
        val api = ServiceGenerator.createService(
            SlackApi::class.java,
            SlackApi.BASE_URL,
            true
        )
        val headers = mutableMapOf(Constants.Common.HEADER_CONTENT_TYPE to Constants.Common.VALUE_FORM_ENCODE)
        withContext(Dispatchers.IO) {
            val response = api.postAction(headers, body).await()
            when (response) {

                is com.ramukaka.network.Success -> {
                    LOGGER.info("message successfully sent")
                }
                else -> {
                    LOGGER.info("message sending failed")
                }
            }.exhaustive
        }
    }

    private suspend fun sendMessageEphemeral(
        message: String,
        channelId: String,
        userId: String,
        attachments: List<Attachment>?
    ) {
        val body = mutableMapOf<String, String?>()
        body[Constants.Slack.ATTACHMENTS] = gson.toJson(attachments)
        body[Constants.Slack.TEXT] = message
        body[Constants.Slack.USER] = userId
        body[Constants.Slack.CHANNEL] = channelId
        body[Constants.Slack.TOKEN] = slackBotToken
        val api = ServiceGenerator.createService(
            SlackApi::class.java,
            SlackApi.BASE_URL,
            true
        )
        val headers = mutableMapOf(Constants.Common.HEADER_CONTENT_TYPE to Constants.Common.VALUE_FORM_ENCODE)
        withContext(Dispatchers.IO) {
            val response = api.sendMessageEphemeral(headers, body).await()
            when (response) {

                is com.ramukaka.network.Success -> {
                    LOGGER.info(response.data.toString())
                }
                is com.ramukaka.network.Failure -> {
                    LOGGER.info(response.errorBody.toString())
                }
                is CallError -> {
                    LOGGER.info(response.throwable?.message)

                }
            }.exhaustive
        }
    }

    suspend fun sendShowSubscriptionDialog(
        branches: List<Branch>?,
        triggerId: String
    ) {
        val branchList = mutableListOf<Element.Option>()
        branches?.forEach { branch ->
            branchList.add(Element.Option(branch.branchName, branch.branchName))
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
        val api = ServiceGenerator.createService(
            SlackApi::class.java,
            SlackApi.BASE_URL,
            true
        )
        val headers = mutableMapOf(Constants.Common.HEADER_CONTENT_TYPE to Constants.Common.VALUE_FORM_ENCODE)

        withContext(Dispatchers.IO) {
            when (val response = api.sendActionOpenDialog(headers, body).await()) {
                is com.ramukaka.network.Success -> {
                    LOGGER.info(response.data.toString())
                }
                is com.ramukaka.network.Failure -> {
                    LOGGER.info(response.errorBody.toString())
                }
                is CallError -> {
                    LOGGER.info(response.throwable?.message)

                }
            }.exhaustive
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

        val body = mutableMapOf<String, String?>()
        body[Constants.Slack.DIALOG] = gson.toJson(dialog)
        body[Constants.Slack.TOKEN] = slackBotToken
        body[Constants.Slack.TRIGGER_ID] = triggerId
        val api = ServiceGenerator.createService(
            SlackApi::class.java,
            SlackApi.BASE_URL,
            true
        )
        val headers = mutableMapOf(Constants.Common.HEADER_CONTENT_TYPE to Constants.Common.VALUE_FORM_ENCODE)

        withContext(Dispatchers.IO) {
            when (val response = api.sendActionOpenDialog(headers, body).await()) {
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


    suspend fun sendShowGenerateApkDialog(
        branches: List<String>?,
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
            branchList.add(Element.Option(branch, branch))
        }
        val defaultValue: String? = if (branches?.size == 1) branches[0] else null
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

        val body = mutableMapOf<String, String?>()
        body[Constants.Slack.DIALOG] = gson.toJson(dialog)
        body[Constants.Slack.TOKEN] = slackBotToken
        body[Constants.Slack.TRIGGER_ID] = triggerId
        val api = ServiceGenerator.createService(
            SlackApi::class.java,
            SlackApi.BASE_URL,
            true
        )
        val headers = mutableMapOf(Constants.Common.HEADER_CONTENT_TYPE to Constants.Common.VALUE_FORM_ENCODE)

        withContext(Dispatchers.IO) {
            when (val response = api.sendActionOpenDialog(headers, body).await()) {
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