package com.tombspawn.slackbot

import com.google.gson.Gson
import com.tombspawn.data.Database
import com.tombspawn.models.config.App
import com.tombspawn.models.locations.Slack
import com.tombspawn.models.slack.SlackEvent
import com.tombspawn.utils.Constants
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import kotlinx.coroutines.CoroutineScope
import com.tombspawn.models.slack.Event
import org.slf4j.LoggerFactory

val LOGGER = LoggerFactory.getLogger("com.application.slack.routing")


fun Routing.slackEvent(database: Database, slackClient: SlackClient) {
    post<Slack.Event> {
        val slackEvent = call.receive<SlackEvent>()
        println(slackEvent.toString())
        when (slackEvent.type) {
            Event.EventType.URL_VERIFICATION -> call.respond(slackEvent)
            Event.EventType.RATE_LIMIT -> {
                call.respond(HttpStatusCode.OK)
                LOGGER.error("Slack Api Rate Limit")
                println("Api rate limit")
            }
            Event.EventType.EVENT_CALLBACK -> {
                call.respond(HttpStatusCode.OK)
                slackClient.subscribeSlackEvent(database, slackEvent)
            }
            Event.EventType.INTERACTIVE_MESSAGE -> {

            }
            else -> {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

fun Routing.slackAction(
    database: Database,
    slackClient: SlackClient,
    baseUrl: String?,
    apps: List<App>,
    gson: Gson
) {
    post<Slack.Action> {
        val params = call.receive<Parameters>()
        val payload = params["payload"]
        println(payload)
        val slackEvent = gson.fromJson<SlackEvent>(payload, SlackEvent::class.java)
        println(slackEvent.toString())

        call.respond(HttpStatusCode.OK)
        when (slackEvent.type) {
            Event.EventType.INTERACTIVE_MESSAGE -> {
                when (slackEvent.callbackId) {
                    // For standup bot
                    Constants.Slack.CALLBACK_STANDUP_MESSAGE -> {
                        showStandupPopup(slackClient, slackEvent)
                    }
                    else -> {
                        slackEvent.actions?.forEach { action ->
                            when {
                                // User confirmed APK Generation from dialog box
                                action.name?.startsWith(Constants.Slack.CALLBACK_CONFIRM_GENERATE_APK, true) == true -> {
                                    subscriptionResponse(action, slackClient, slackEvent, database, apps, gson)
                                }
                            }
                        }
                    }
                }
            }
            Event.EventType.MESSAGE_ACTION -> {
                when (slackEvent.callbackId) {
                    Constants.Slack.TYPE_CREATE_MOCK_API -> {
                        slackClient.sendShowCreateApiDialog(database.getVerbs(), slackEvent.triggerId!!)
                    }
                }
            }
            Event.EventType.DIALOG_SUBMISSION -> {
                onDialogSubmitted(apps, slackEvent, slackClient, database, baseUrl ?: "", gson)
            }
        }
    }
}

fun CoroutineScope.onDialogSubmitted(apps: List<App>, slackEvent: SlackEvent, slackClient: SlackClient,
                                     database: Database, baseUrl: String, gson: Gson) {
    when {
        slackEvent.callbackId?.startsWith(Constants.Slack.CALLBACK_SUBSCRIBE_CONSUMER) == true -> {
            sendSubscribeToBranch(slackEvent, slackClient, database, apps)
        }
        slackEvent.callbackId == Constants.Slack.CALLBACK_CREATE_API -> {
            createApiDialogResponse(slackClient, slackEvent, database, baseUrl)
        }
        slackEvent.callbackId?.startsWith(Constants.Slack.CALLBACK_GENERATE_APK) == true -> {
            generateAppDialogResponse(slackClient, slackEvent, apps, gson)
        }
    }
}