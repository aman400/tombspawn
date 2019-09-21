package com.ramukaka.slackbot

import com.ramukaka.models.locations.Slack
import com.ramukaka.models.slack.SlackEvent
import com.ramukaka.models.slack.attachment
import com.ramukaka.utils.Constants
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.post
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import models.slack.Action
import models.slack.action

fun Routing.standup(slackClient: SlackClient) {
    post<Slack.Standup> {
        val params = call.receiveParameters()

        val channel = params[Constants.Slack.CHANNEL_ID]
        params.forEach { key, list ->
            LOGGER.info("$key: $list")
        }

        launch(Dispatchers.IO) {
            val attachment = attachment {
                callbackId = Constants.Slack.CALLBACK_STANDUP_MESSAGE
                fallback = "Please post your standup updates"
                text = "Please post your standup updates."
                id = 1
                color = "#0000FF"
                actions {
                    +action {
                        confirm = null
                        name = Constants.Slack.CALLBACK_STANDUP_DIALOG
                        text = "Update"
                        type = Action.ActionType.BUTTON
                        style = Action.ActionStyle.PRIMARY
                    }
                }
            }
            slackClient.sendMessage("Please update your standup notes", channel!!, listOf(attachment))
        }
        call.respond(HttpStatusCode.OK)
    }
}

fun CoroutineScope.showStandupPopup(slackClient: SlackClient, slackEvent: SlackEvent) {
    // Handle only single action
    val dialog = com.ramukaka.models.slack.dialog {
        callbackId = com.ramukaka.utils.Constants.Slack.CALLBACK_STANDUP_DIALOG
        title = "Standup notes"
        submitLabel = "Submit"
        notifyOnCancel = false
        elements {
            // Add text are for what person did on last working day
            +com.ramukaka.models.slack.element {
                type = com.ramukaka.models.slack.ElementType.TEXT_AREA
                label = "What did you do on your last working day?"
                hint = "For eg: I did nothing yesterday, I regret it today."
                name = "yesterday"
                maxLength = 3000
            }
            // Add text are for what person is going to do today
            +com.ramukaka.models.slack.element {
                type = com.ramukaka.models.slack.ElementType.TEXT_AREA
                label = "What will you do today?"
                hint =
                    "For eg: Today I will be wasting most of my time by laughing and gossiping around."
                name = "today"
                maxLength = 3000
            }
        }
    }
    launch(Dispatchers.IO) {
        slackClient.sendShowDialog(dialog, slackEvent.triggerId!!)
    }
}