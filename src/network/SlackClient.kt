package com.ramukaka.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ramukaka.data.Database
import com.ramukaka.extensions.execute
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
import kotlinx.coroutines.launch
import models.slack.Action
import models.slack.Option
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.QueryMap
import java.io.File
import java.util.logging.Logger

interface SlackClient {
    companion object {
        const val BASE_URL = "https://slack.com"
        const val PARAM_TOKEN = "token"
        const val PARAM_USER_ID = "user"
    }

    @GET("/api/users.profile.get")
    fun getProfile (@QueryMap queryMap: MutableMap<String, String>): Observable<Response<SlackProfileResponse>>
}

private const val OUTPUT_SEPARATOR = "##***##"
private const val ARA_OUTPUT_SEPARATOR = "OUTPUT_SEPARATOR"
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

                slackEvent.event?.let { event ->
                    if (!database.userExists(event.user)) {
                        event.user?.let { user ->
                            fetchUser(user, authToken, database)
                        }
                    }
                    when (event.type) {
                        Constants.Slack.EVENT_TYPE_APP_MENTION -> {
                        }
                        Constants.Slack.EVENT_TYPE_MESSAGE -> {
                        }
                        else -> {

                        }
                    }
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

                val branches = fetchAllBranches(gradlePath, consumerAppDir)
                val branchList = mutableListOf<Option>()
                branches?.forEach { branchName ->
                    branchList.add(Option(branchName, branchName))
                }
                val attachments = mutableListOf(
                    Attachment(
                        Constants.Slack.SUBSCRIBE_GENERATE_APK, "Subscribe to github branch for code changes.",
                        "Select the branch to subscribe the changes for APK Generation.", 1, "#0000FF",
                        mutableListOf(
                            Action(
                                confirm = null,
                                name = "choose_branch",
                                text = "Choose the branch to subscribe for changes",
                                type = "select",
                                options = branchList
                            )
                        )
                    )
                )

                val gson = Gson()

                val body = mutableMapOf<String, String?>()
                body[Constants.Slack.ATTACHMENTS] = gson.toJson(attachments)
                body[Constants.Slack.TEXT] = "Generate an APK"
                body[Constants.Slack.CHANNEL] = slackEvent.channel?.id
                body[Constants.Slack.TOKEN] = slackAuthToken
                val api = ServiceGenerator.createService(RamukakaApi::class.java, SlackClient.BASE_URL, true)
                val headers = mutableMapOf(Constants.Common.HEADER_CONTENT_TYPE to Constants.Common.VALUE_FORM_ENCODE)
                api.postAction(headers, body).enqueue(object : Callback<JsonObject> {
                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        println("post failure")
                    }

                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        println("post success")
                    }

                })
            }
        }
    }
}

private fun fetchAllBranches(gradlePath: String, dirName: String): List<String>? {
    val executableCommand = "$gradlePath fetchRemoteBranches -P$ARA_OUTPUT_SEPARATOR=$OUTPUT_SEPARATOR"
    val response = executableCommand.execute(File(dirName))
    response?.let {
        val parsedResponse = it.split(OUTPUT_SEPARATOR)
        if (parsedResponse.size >= 2) {
            return parsedResponse[1].split("\n")
        }
    }
    return null
}

private fun fetchUser(userId: String, authToken: String, database: Database) = run {
    val api = ServiceGenerator.createService(
        SlackClient::class.java, SlackClient.BASE_URL,
        true, callAdapterFactory = RxJava2CallAdapterFactory.create()
    )
    val queryParams = mutableMapOf<String, String>()
    queryParams[SlackClient.PARAM_TOKEN] = authToken
    queryParams[SlackClient.PARAM_USER_ID] = userId
    api.getProfile(queryParams).subscribe({ response ->
        if (response.isSuccessful) {
            response.body()?.let { body ->
                if (body.success) {
                    database.addUser(body.user, userId)
                }
            }
        }
    }, { throwable ->
        LOGGER.log(java.util.logging.Level.SEVERE, throwable.message, throwable!!)
    })
}