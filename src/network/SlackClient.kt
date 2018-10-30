package com.ramukaka.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ramukaka.data.Database
import com.ramukaka.extensions.execute
import com.ramukaka.models.ErrorResponse
import com.ramukaka.models.locations.Slack
import com.ramukaka.models.slack.Attachment
import com.ramukaka.models.slack.SlackEvent
import com.ramukaka.models.slack.SlackProfileResponse
import com.ramukaka.utils.Constants
import io.ktor.application.call
import io.ktor.http.Parameters
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import models.slack.Action
import models.slack.BotInfo
import models.slack.Option
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.*
import java.io.File
import java.util.logging.Logger

interface SlackApi {
    companion object {
        const val BASE_URL = "https://slack.com"
        const val PARAM_TOKEN = "token"
        const val PARAM_USER_ID = "user"
    }

    @GET("/api/users.profile.get")
    fun getProfile(@QueryMap queryMap: MutableMap<String, String>): Observable<Response<SlackProfileResponse>>

    @FormUrlEncoded
    @POST("/api/chat.postMessage")
    fun postAction(@HeaderMap headers: MutableMap<String, String>, @FieldMap body: MutableMap<String, String?>): Observable<Response<JsonObject>>

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
    fun sendError(
        @HeaderMap header: MutableMap<String, String>, @Url url: String,
        @Body errorResponse: ErrorResponse
    ): Call<String>

    @GET("/api/rtm.connect")
    fun fetchBotInfo(
        @HeaderMap headers: MutableMap<String, String>,
        @Query("token") botToken: String
    ): Observable<Response<BotInfo>>
}

private val LOGGER = Logger.getLogger("SlackClient")

fun Routing.subscribe() {
    post<Slack.Subscribe> {
        call.respond("")
    }
}

fun Routing.slackEvent(authToken: String, database: Database, gradlePath: String, consumerAppDir: String) {
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
                    subscribeSlackEvent(authToken, database, slackEvent, gradlePath, consumerAppDir)
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

fun Routing.slackAction(slackAuthToken: String, consumerAppDir: String, gradlePath: String) {
    post<Slack.Action> {
        val params = call.receive<Parameters>()
        val payload = params["payload"]
        val slackEvent = Gson().fromJson<SlackEvent>(payload, SlackEvent::class.java)
        println(slackEvent.toString())

        call.respond("")
        if (slackEvent.callbackId.equals(Constants.Slack.SUBSCRIBE_GENERATE_APK)) {

        } else {
            launch {

            }
        }
    }
}

private suspend fun fetchUser(userId: String, authToken: String, database: Database) = run {
    val api = ServiceGenerator.createService(
        SlackApi::class.java, SlackApi.BASE_URL,
        true, callAdapterFactory = RxJava2CallAdapterFactory.create()
    )
    val queryParams = mutableMapOf<String, String>()
    queryParams[SlackApi.PARAM_TOKEN] = authToken
    queryParams[SlackApi.PARAM_USER_ID] = userId
    api.getProfile(queryParams).subscribe({ response ->
        if (response.isSuccessful) {
            response.body()?.let { body ->
                if (body.success) {
                    GlobalScope.launch {
                        database.addUser(userId, body.user.name, body.user.email, Constants.Database.USER_TYPE_USER)
                    }
                }
            }
        }
    }, { throwable ->
        LOGGER.log(java.util.logging.Level.SEVERE, throwable.message, throwable!!)
    })
}


private suspend fun subscribeSlackEvent(authToken: String, database: Database, slackEvent: SlackEvent,
                                        gradlePath: String, consumerAppDir: String) {
    slackEvent.event?.let { event ->
        if (!database.userExists(event.user)) {
            event.user?.let { user ->
                fetchUser(user, authToken, database)
            }
        }
        when (event.type) {
            Constants.Slack.EVENT_TYPE_APP_MENTION, Constants.Slack.EVENT_TYPE_MESSAGE -> {
                GlobalScope.launch {
                    val user = database.getUser(Constants.Database.USER_TYPE_BOT)
                    user?.let { bot ->
                        when (event.text?.substringAfter("<@${bot.slackId}>", event.text)?.trim()) {
                            Constants.Slack.TYPE_SUBSCRIBE_CONSUMER -> {
                                println("Valid Event Consumer")
//                                fetchAllBranches(gradlePath, consumerAppDir)?.let { branches ->
//                                    sendChooseBranchAction(branches, slackEvent, authToken)
//                                }
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
    val api = ServiceGenerator.createService(SlackApi::class.java, SlackApi.BASE_URL, true, callAdapterFactory = RxJava2CallAdapterFactory.create())
    val headers = mutableMapOf(Constants.Common.HEADER_CONTENT_TYPE to Constants.Common.VALUE_FORM_ENCODE)
    api.postAction(headers, body).subscribeOn(Schedulers.io()).subscribe({
        if(it.isSuccessful) {
            println("post success")
        }
    }, {
        it.printStackTrace()
        println("post failure")
    })
}