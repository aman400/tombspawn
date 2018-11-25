package com.ramukaka.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ramukaka.data.Branch
import com.ramukaka.data.Database
import com.ramukaka.extensions.execute
import com.ramukaka.extensions.random
import com.ramukaka.extensions.toMap
import com.ramukaka.models.RequestData
import com.ramukaka.models.locations.Slack
import com.ramukaka.models.slack.*
import com.ramukaka.utils.Constants
import io.ktor.application.call
import io.ktor.http.Parameters
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import models.slack.Action
import models.slack.BotInfo
import models.slack.Confirm
import models.slack.Option
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.*
import java.io.File
import java.util.logging.Logger
import kotlin.coroutines.coroutineContext


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

private val LOGGER = Logger.getLogger("SlackClient")

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

fun Routing.slackAction(database: Database, slackClient: SlackClient, consumerAppDir: String) {
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
                        Constants.Slack.CALLBACK_CONFIRM_GENERATE_APK -> {
                            if (action.value!!.toBoolean()) {
                                slackClient.sendShowGenerateApkDialog(
                                    null, null, null,
                                    slackEvent.triggerId!!
                                )
                            } else {
                                println("Not generating the APK")
                            }
                        }
                    }
                }
            }
            Constants.Slack.EVENT_TYPE_MESSAGE_ACTION -> {
                when (slackEvent.callbackId) {
                    Constants.Slack.TYPE_SUBSCRIBE_CONSUMER ->
                        launch {
                            slackClient.sendShowSubscriptionDialog(
                                database.getBranches(Constants.Common.APP_CONSUMER),
                                slackEvent.triggerId!!
                            )
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
                                val channel = slackEvent.channel?.id
                                if (branch != null && channel != null) {

                                    if (slackEvent.responseUrl != null) {
                                        if (database.subscribeUser(
                                                userId,
                                                Constants.Common.APP_CONSUMER,
                                                branch,
                                                channel
                                            )
                                        ) {
                                            launch(coroutineContext) {
                                                slackClient.sendMessage(
                                                    slackEvent.responseUrl,
                                                    RequestData(response = "You are successfully subscribed to $branch")
                                                )
                                            }
                                        } else {
                                            launch(coroutineContext) {
                                                slackClient.sendMessage(
                                                    slackEvent.responseUrl,
                                                    RequestData(response = "You are already subscribed to $branch")
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

                    Constants.Slack.CALLBACK_GENERATE_APK -> {
                        slackClient.generateAndUploadApk(
                            slackEvent.dialogResponse, slackEvent.channel?.id ?: "general", consumerAppDir, slackEvent.responseUrl)
                    }
                }
            }
        }
    }
}

fun Routing.buildConsumer(appDir: String, slackClient: SlackClient) {
    post<Slack.Consumer> {
        val params = call.receiveParameters()

        val channelId = params["channel_id"]
        val text = params["text"]
        val responseUrl = params["response_url"]

        text?.trim()?.toMap()?.let { buildData ->
            slackClient.generateAndUploadApk(buildData, channelId!!, appDir, responseUrl)
        }
            ?: call.respond("Invalid command. Usage: '/build BRANCH=<git-branch-name>(optional)  BUILD_TYPE=<release/debug>(optional)  FLAVOUR=<flavour>(optional)'.")
    }
}


fun Routing.buildFleet(appDir: String, slackClient: SlackClient) {
    post<Slack.Fleet> {
        val params = call.receiveParameters()

        val channelId = params["channel_id"]
        val text = params["text"]
        val responseUrl = params["response_url"]
//        val triggerId = params["trigger_id"]

        text?.trim()?.toMap()?.let { buildData ->
            slackClient.generateAndUploadApk(buildData, channelId!!, appDir, responseUrl)
            call.respond(randomWaitingMessages.random()!!)
        }
            ?: call.respond("Invalid command. Usage: '/build BRANCH=<git-branch-name>(optional)  BUILD_TYPE=<release/debug>(optional)  FLAVOUR=<flavour>(optional)'.")
    }
}

class SlackClient(
    private val slackAuthToken: String, private val slackBotToken: String,
    private val gradlePath: String, private val uploadDirPath: String
) {
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

        @FormUrlEncoded
        @GET("/api/channels.info")
        fun getChannelInfo(@QueryMap queryParams: Map<String, String>): Call<ChannelInfoResponse>

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
        ): Call<JsonObject>

        @GET("/api/rtm.connect")
        fun fetchBotInfo(
            @HeaderMap headers: MutableMap<String, String>,
            @Query("token") botToken: String
        ): Call<BotInfo>
    }

    suspend fun generateAndUploadApk(
        buildData: Map<String, String>?,
        channelId: String,
        appDir: String,
        responseUrl: String? = null
    ) {
        val APKPrefix = System.currentTimeMillis()

        var executableCommand =
            "$gradlePath assembleWithArgs -PFILE_PATH=$uploadDirPath -PAPP_PREFIX=$APKPrefix"

        buildData?.forEach { key, value ->
            executableCommand += " -P$key=$value"
        }

        GlobalScope.launch(coroutineContext) {
            if(responseUrl != null) {
                sendMessage(randomWaitingMessages.random()!!, channelId, null)
            }
            println(executableCommand)
            val commandResponse = executableCommand.execute(File(appDir))

            val tempDirectory = File(uploadDirPath)
            if (tempDirectory.exists()) {
                val firstFile = tempDirectory.listFiles { _, name ->
                    name.contains("$APKPrefix", true)
                }.firstOrNull()
                firstFile?.let { file ->
                    if (file.exists()) {
                        uploadFile(file, channelId)
                    } else {
                        if (responseUrl != null) {
                            sendMessage(
                                responseUrl,
                                RequestData(
                                    response = commandResponse ?: "Something went wrong. Unable to generate the APK"
                                )
                            )
                        } else {
                            sendMessage(
                                commandResponse ?: "Something went wrong. Unable to generate the APK",
                                channelId,
                                null
                            )
                        }
                    }
                } ?: if (responseUrl != null) {
                    sendMessage(
                        responseUrl,
                        RequestData(response = commandResponse ?: "Something went wrong. Unable to generate the APK")
                    )
                } else {
                    sendMessage(commandResponse ?: "Something went wrong. Unable to generate the APK", channelId, null)
                }
            } else {
                if (responseUrl != null) {
                    sendMessage(
                        responseUrl,
                        RequestData(response = commandResponse ?: "Something went wrong. Unable to generate the APK")
                    )
                } else {
                    sendMessage(commandResponse ?: "Something went wrong. Unable to generate the APK", channelId, null)
                }
            }
        }
    }

    suspend fun sendMessage(url: String, data: RequestData?) {
        val headers = mutableMapOf("Content-type" to "application/json")
        val api = ServiceGenerator.createService(SlackApi::class.java, isLoggingEnabled = true)
        GlobalScope.launch(coroutineContext) {
            val response = api.sendMessage(headers, url, data).execute()
            if (response.isSuccessful) {
                println(response.body())
            } else {
                println(response.errorBody())
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
        GlobalScope.launch(coroutineContext) {
            val response = call.execute()
            if (response.isSuccessful) {
                println(if (response.body()?.delivered == true) "delivered" else "Not delivered")
            } else {
                println(response.errorBody().toString())
            }
            if (deleteFile)
                file.delete()
        }
    }


    suspend fun fetchUser(userId: String, database: Database) = run {
        val api = ServiceGenerator.createService(
            SlackApi::class.java, SlackApi.BASE_URL,
            true, callAdapterFactory = RxJava2CallAdapterFactory.create()
        )
        val queryParams = mutableMapOf<String, String>()
        queryParams[SlackApi.PARAM_TOKEN] = slackAuthToken
        queryParams[SlackApi.PARAM_USER_ID] = userId
        GlobalScope.launch(coroutineContext) {
            val response = api.getProfile(queryParams).execute()
            if (response.isSuccessful) {
                database.addUser(
                    userId,
                    response.body()?.user?.name,
                    response.body()?.user?.email,
                    Constants.Database.USER_TYPE_USER
                )
            }
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
                    GlobalScope.launch {
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

    suspend fun sendChooseBranchAction(
        branches: List<Branch>?,
        slackEvent: SlackEvent
    ) {
        val branchList = mutableListOf<Option>()
        branches?.forEach { branch ->
            branchList.add(Option(branch.branchName, branch.branchName))
        }
        val attachments = mutableListOf(
            Attachment(
                Constants.Slack.SUBSCRIBE_GENERATE_APK, "Subscribe to github branch for code changes.",
                "Select the branch to subscribe for changes for APK generation.", 1, "#0000FF",
                mutableListOf(
                    Action(
                        confirm = null,
                        name = Constants.Slack.ACTION_CHOOSE_BRANCH,
                        text = "Choose the branch to subscribe for changes.",
                        type = Action.ActionType.SELECT,
                        options = branchList
                    )
                )
            )
        )

        sendMessage("Generate an APK", slackEvent.channel?.id ?: "general", attachments)
    }

    private suspend fun sendMessage(message: String, channelId: String, attachments: List<Attachment>?) {
        val body = mutableMapOf<String, String?>()
        attachments?.let {
            body[Constants.Slack.ATTACHMENTS] = Gson().toJson(attachments)
        }
        body[Constants.Slack.TEXT] = message
        body[Constants.Slack.CHANNEL] = channelId
        body[Constants.Slack.TOKEN] = slackAuthToken
        val api = ServiceGenerator.createService(
            SlackApi::class.java,
            SlackApi.BASE_URL,
            true,
            callAdapterFactory = RxJava2CallAdapterFactory.create()
        )
        val headers = mutableMapOf(Constants.Common.HEADER_CONTENT_TYPE to Constants.Common.VALUE_FORM_ENCODE)
        GlobalScope.launch(coroutineContext) {
            val response = api.postAction(headers, body).execute()
            if (response.isSuccessful) {
                println("Success")
            } else {
                println("Failure")
            }
        }
    }

    private suspend fun sendMessageEphemeral(message: String, channelId: String, userId: String, attachments: List<Attachment>?) {
        val body = mutableMapOf<String, String?>()
        body[Constants.Slack.ATTACHMENTS] = Gson().toJson(attachments)
        body[Constants.Slack.TEXT] = message
        body[Constants.Slack.USER] = userId
        body[Constants.Slack.CHANNEL] = channelId
        body[Constants.Slack.TOKEN] = slackAuthToken
        val api = ServiceGenerator.createService(
            SlackApi::class.java,
            SlackApi.BASE_URL,
            true,
            callAdapterFactory = RxJava2CallAdapterFactory.create()
        )
        val headers = mutableMapOf(Constants.Common.HEADER_CONTENT_TYPE to Constants.Common.VALUE_FORM_ENCODE)
        GlobalScope.launch(coroutineContext) {
            val response = api.sendMessageEphemeral(headers, body).execute()
            if (response.isSuccessful) {
                println(response.body())
            } else {
                println(response.errorBody())
            }
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
        body[Constants.Slack.DIALOG] = Gson().toJson(dialog)
        body[Constants.Slack.TOKEN] = slackAuthToken
        body[Constants.Slack.TRIGGER_ID] = triggerId
        val api = ServiceGenerator.createService(
            SlackApi::class.java,
            SlackApi.BASE_URL,
            true
        )
        val headers = mutableMapOf(Constants.Common.HEADER_CONTENT_TYPE to Constants.Common.VALUE_FORM_ENCODE)

        GlobalScope.launch(coroutineContext) {
            if (api.sendActionOpenDialog(headers, body).execute().isSuccessful) {
                println("post success")
            } else {
                println("post failure")
            }
        }
    }

    suspend fun sendShowConfirmGenerateApk(channelId: String, branch: String, user: String) {
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
                        value = Constants.Slack.ACTION_CONFIRM
                    ),
                    Action(
                        confirm = null,
                        name = Constants.Slack.CALLBACK_CONFIRM_GENERATE_APK,
                        text = "No",
                        type = Action.ActionType.BUTTON,
                        style = Action.ActionStyle.DEFAULT,
                        value = Constants.Slack.ACTION_REJECT
                    )
                ), Attachment.AttachmentType.DEFAULT
            )
        )

        sendMessageEphemeral("New changes are available in `$branch` branch.", channelId, user, attachments)
    }

    suspend fun sendShowGenerateApkDialog(
        branches: List<Branch>?,
        buildTypes: List<String>?,
        flavours: List<String>?,
        triggerId: String
    ) {
        val dialogElementList = mutableListOf<Element>()
        val branchList = mutableListOf<Element.Option>()
        branches?.forEach { branch ->
            branchList.add(Element.Option(branch.branchName, branch.branchName))
        }
        if (branchList.size > 0) {
            dialogElementList.add(
                Element(
                    ElementType.SELECT, "Select Branch",
                    Constants.Slack.TYPE_SELECT_BRANCH, options = branchList
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
                "http://www.google.co.in",
                inputType = Element.InputType.URL
            )
        )

        val dialog = Dialog(
            Constants.Slack.CALLBACK_GENERATE_APK, "Generate APK", "Submit", false, null,
            dialogElementList
        )

        val body = mutableMapOf<String, String?>()
        body[Constants.Slack.DIALOG] = Gson().toJson(dialog)
        body[Constants.Slack.TOKEN] = slackAuthToken
        body[Constants.Slack.TRIGGER_ID] = triggerId
        val api = ServiceGenerator.createService(
            SlackApi::class.java,
            SlackApi.BASE_URL,
            true
        )
        val headers = mutableMapOf(Constants.Common.HEADER_CONTENT_TYPE to Constants.Common.VALUE_FORM_ENCODE)

        GlobalScope.launch(coroutineContext) {
            if (api.sendActionOpenDialog(headers, body).execute().isSuccessful) {
                println("post success")
            } else {
                println("post failure")
            }
        }

    }
}