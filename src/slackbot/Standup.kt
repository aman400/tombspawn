package com.ramukaka.slackbot

import com.ramukaka.models.locations.Slack
import com.ramukaka.models.slack.attachment
import com.ramukaka.utils.Constants
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.post
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
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