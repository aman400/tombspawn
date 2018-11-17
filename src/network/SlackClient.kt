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
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import models.slack.Action
import models.slack.BotInfo
import models.slack.Option
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.*
import java.io.File
import java.util.logging.Logger
import kotlin.coroutines.coroutineContext

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
    fun postAction(@HeaderMap headers: MutableMap<String, String>, @FieldMap body: MutableMap<String, String?>): Observable<Response<JsonObject>>

    @FormUrlEncoded
    @POST("/api/dialog.open")
    fun sendActionOpenDialog(@HeaderMap headers: MutableMap<String, String>, @FieldMap body: MutableMap<String, String?>): Observable<Response<JsonObject>>

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
        @Body requestBody: RequestData
    ): Call<String>

    @GET("/api/rtm.connect")
    fun fetchBotInfo(
        @HeaderMap headers: MutableMap<String, String>,
        @Query("token") botToken: String
    ): Observable<Response<BotInfo>>
}

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

fun Routing.slackEvent(authToken: String, database: Database) {
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
                    subscribeSlackEvent(authToken, database, slackEvent)
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

fun Routing.slackAction(database: Database, authToken: String) {
    post<Slack.Action> {
        val params = call.receive<Parameters>()
        val payload = params["payload"]
        val slackEvent = Gson().fromJson<SlackEvent>(payload, SlackEvent::class.java)
        println(slackEvent.toString())

        call.respond("")
        when (slackEvent.type) {
            Constants.Slack.EVENT_TYPE_MESSAGE_ACTION -> {
                when (slackEvent.callbackId) {
                    Constants.Slack.TYPE_SUBSCRIBE_CONSUMER -> sendShowSubscriptionDialog(
                        database.getBranches(Constants.Common.APP_CONSUMER),
                        authToken, slackEvent.triggerId!!
                    )

                }
            }
            Constants.Slack.EVENT_TYPE_DIALOG -> {
                when (slackEvent.callbackId) {
                    Constants.Slack.CALLBACK_SUBSCRIBE_CONSUMER -> {
                        launch {
                            slackEvent.user?.id?.let { userId ->
                                if (!database.userExists(userId)) {
                                    runBlocking {
                                        fetchUser(userId, authToken, database)
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
                                                sendMessage(
                                                    slackEvent.responseUrl,
                                                    RequestData(response = "You are successfully subscribed to branch: $branch")
                                                )
                                            }
                                        } else {
                                            launch(coroutineContext) {
                                                sendMessage(
                                                    slackEvent.responseUrl,
                                                    RequestData(response = "You are already subscribed to branch: $branch")
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
                }
            }
        }

        if (slackEvent.callbackId.equals(Constants.Slack.SUBSCRIBE_GENERATE_APK)) {

        } else {
            launch {

            }
        }
    }
}

fun Routing.buildConsumer(gradlePath: String, uploadDirPath: String, consumerAppDir: String, token: String) {
    post<Slack.Consumer> {
        val params = call.receiveParameters()

        val channelId = params["channel_id"]
        val text = params["text"]
        val responseUrl = params["response_url"]
        val APKPrefix = System.currentTimeMillis()

        text?.trim()?.toMap()?.let { buildData ->
            var executableCommand =
                "$gradlePath assembleWithArgs -PFILE_PATH=$uploadDirPath -PAPP_PREFIX=$APKPrefix"

            buildData.forEach { key, value ->
                executableCommand += " -P$key=$value"
            }

            launch {
                println(executableCommand)
                val commandResponse = executableCommand.execute(File(consumerAppDir))

                val tempDirectory = File(uploadDirPath)
                if (tempDirectory.exists()) {
                    val firstFile = tempDirectory.listFiles { dir, name ->
                        name.contains("$APKPrefix", true)
                    }.firstOrNull()
                    firstFile?.let { file ->
                        if (file.exists()) {
                            uploadFile(file, channelId!!, token)
                        } else sendMessage(commandResponse, responseUrl!!)
                    } ?: sendMessage(commandResponse, responseUrl!!)
                } else {
                    sendMessage(commandResponse, responseUrl!!)
                }
            }
            call.respond(randomWaitingMessages.random()!!)
        }
            ?: call.respond("Invalid command. Usage: '/build BRANCH=<git-branch-name>(optional)  BUILD_TYPE=<release/debug>(optional)  FLAVOUR=<flavour>(optional)'.")
    }
}


fun Routing.buildFleet(
    database: Database,
    gradlePath: String,
    uploadDirPath: String,
    fleetAppDir: String,
    token: String
) {
    post<Slack.Fleet> {
        val params = call.receiveParameters()

        val channelId = params["channel_id"]
        val text = params["text"]
        val APKPrefix = System.currentTimeMillis()
        val responseUrl = params["response_url"]
        val triggerId = params["trigger_id"]

        text?.trim()?.toMap()?.let { buildData ->
            var executableCommand =
                "$gradlePath assembleWithArgs -PFILE_PATH=$uploadDirPath -PAPP_PREFIX=$APKPrefix"

            buildData.forEach { key, value ->
                executableCommand += " -P$key=$value"
            }

            launch {
                println(executableCommand)
                val commandResponse = executableCommand.execute(File(fleetAppDir))

                val tempDirectory = File(uploadDirPath)
                if (tempDirectory.exists()) {
                    val firstFile = tempDirectory.listFiles { dir, name ->
                        name.contains("$APKPrefix", true)
                    }.firstOrNull()
                    firstFile?.let { file ->
                        if (file.exists()) {
                            uploadFile(file, channelId!!, token)
                        } else sendMessage(commandResponse, responseUrl!!)
                    } ?: sendMessage(commandResponse, responseUrl!!)
                } else {
                    sendMessage(commandResponse, responseUrl!!)
                }
            }
            call.respond(randomWaitingMessages.random()!!)
        }
            ?: call.respond("Invalid command. Usage: '/build BRANCH=<git-branch-name>(optional)  BUILD_TYPE=<release/debug>(optional)  FLAVOUR=<flavour>(optional)'.")
    }
}

private fun sendMessage(commandResponse: String?, responseUrl: String) {
    val errorResponse = if (!commandResponse.isNullOrEmpty()) {
        RequestData(response = commandResponse)
    } else {
        RequestData(response = "Something went wrong. Unable to generate APK.")
    }

    val api = ServiceGenerator.createService(SlackApi::class.java, SlackApi.BASE_URL, true)
    val headers = mutableMapOf("Content-type" to "application/json")
    val call = api.sendMessage(headers, responseUrl, errorResponse)
    call.enqueue(object : Callback<String> {
        override fun onFailure(call: Call<String>, throwable: Throwable) {
            throwable.printStackTrace()
        }

        override fun onResponse(
            call: Call<String>,
            response: Response<String>
        ) {
            if (response.isSuccessful) {
                println(response.body())
            }
        }
    })
}


private fun uploadFile(file: File, channelId: String, token: String, deleteFile: Boolean = true) {
    val requestBody =
        RequestBody.create(MediaType.parse(ServiceGenerator.MULTIPART_FORM_DATA), file)
    val multipartBody =
        MultipartBody.Part.createFormData("file", "App-debug.apk", requestBody)

    val appToken = RequestBody.create(
        okhttp3.MultipartBody.FORM,
        token
    )
    val title = RequestBody.create(okhttp3.MultipartBody.FORM, file.nameWithoutExtension)
    val filename = RequestBody.create(okhttp3.MultipartBody.FORM, file.name)
    val fileType = RequestBody.create(okhttp3.MultipartBody.FORM, "auto")
    val channels = RequestBody.create(okhttp3.MultipartBody.FORM, channelId)

    val api = ServiceGenerator.createService(SlackApi::class.java, SlackApi.BASE_URL, false)
    val call = api.pushApp(appToken, title, filename, fileType, channels, multipartBody)
    val response = call.execute()
    if (response.isSuccessful) {
        println(if (response.body()?.delivered == true) "delivered" else "Not delivered")
    } else {
        println(response.errorBody().toString())
    }
    if (deleteFile)
        file.delete()
}


private suspend fun fetchUser(userId: String, authToken: String, database: Database) = run {
    val api = ServiceGenerator.createService(
        SlackApi::class.java, SlackApi.BASE_URL,
        true, callAdapterFactory = RxJava2CallAdapterFactory.create()
    )
    val queryParams = mutableMapOf<String, String>()
    queryParams[SlackApi.PARAM_TOKEN] = authToken
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


private suspend fun subscribeSlackEvent(
    authToken: String, database: Database, slackEvent: SlackEvent
) {
    slackEvent.event?.let { event ->
        if (!database.userExists(event.user)) {
            event.user?.let { user ->
                runBlocking {
                    fetchUser(user, authToken, database)
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

private fun sendChooseBranchAction(branches: List<String>, slackEvent: SlackEvent, slackAuthToken: String) {
    val branchList = mutableListOf<Option>()
    branches.forEach { branchName ->
        branchList.add(Option(branchName, branchName))
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
                    type = Constants.Slack.ATTACHMENT_TYPE_SELECT,
                    options = branchList
                )
            )
        )
    )

    val body = mutableMapOf<String, String?>()
    body[Constants.Slack.ATTACHMENTS] = Gson().toJson(attachments)
    body[Constants.Slack.TEXT] = "Generate an APK"
    body[Constants.Slack.CHANNEL] = slackEvent.channel?.id ?: slackEvent.event!!.channel
    body[Constants.Slack.TOKEN] = slackAuthToken
    val api = ServiceGenerator.createService(
        SlackApi::class.java,
        SlackApi.BASE_URL,
        true,
        callAdapterFactory = RxJava2CallAdapterFactory.create()
    )
    val headers = mutableMapOf(Constants.Common.HEADER_CONTENT_TYPE to Constants.Common.VALUE_FORM_ENCODE)
    api.postAction(headers, body).subscribeOn(Schedulers.io()).subscribe({
        if (it.isSuccessful) {
            println("post success")
        }
    }, {
        it.printStackTrace()
        println("post failure")
    })
}

suspend fun sendMessage(url: String, data: RequestData) {
    val headers = mutableMapOf("Content-type" to "application/json")
    val api = ServiceGenerator.createService(SlackApi::class.java, isLoggingEnabled = true)
    GlobalScope.launch(coroutineContext) { api.sendMessage(headers, url, data).execute().isSuccessful }
}

private fun sendShowSubscriptionDialog(
    branches: List<Branch>?,
    slackAuthToken: String,
    triggerId: String
) {
    val branchList = mutableListOf<Element.Option>()
    branches?.forEach { branch ->
        branchList.add(Element.Option(branch.branchName, branch.branchName))
    }
    val dialog = Dialog(
        Constants.Slack.CALLBACK_SUBSCRIBE_CONSUMER, "Subscription Details", "Submit", false, null,
        mutableListOf(
            Element(ElementType.SELECT, "Select Branch", Constants.Slack.TYPE_SELECT_BRANCH, options = branchList),
            Element(
                ElementType.TEXT,
                "App URL",
                Constants.Slack.TYPE_SELECT_URL,
                "http://www.google.co.in",
                inputType = Element.InputType.URL
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
        true,
        callAdapterFactory = RxJava2CallAdapterFactory.create()
    )
    val headers = mutableMapOf(Constants.Common.HEADER_CONTENT_TYPE to Constants.Common.VALUE_FORM_ENCODE)
    api.sendActionOpenDialog(headers, body).subscribeOn(Schedulers.io()).subscribe({
        if (it.isSuccessful) {
            println("post success")
        }
    }, {
        it.printStackTrace()
        println("post failure")
    })
}