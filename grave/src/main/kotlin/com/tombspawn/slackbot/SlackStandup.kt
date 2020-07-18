package com.tombspawn.slackbot

import com.tombspawn.models.slack.ElementType
import com.tombspawn.models.slack.dialog
import com.tombspawn.models.slack.element
import com.tombspawn.utils.Constants

suspend fun SlackService.showStandupPopup(triggerId: String) {
    // Handle only single action
    val dialog = dialog {
        callbackId = Constants.Slack.CALLBACK_STANDUP_DIALOG
        title = "Standup notes"
        submitLabel = "Submit"
        notifyOnCancel = false
        elements {
            // Add text are for what person did on last working day
            +element {
                type = ElementType.TEXT_AREA
                label = "What did you do on your last working day?"
                hint = "For eg: I did nothing yesterday, I regret it today."
                name = "yesterday"
                maxLength = 3000
            }
            // Add text are for what person is going to do today
            +element {
                type = ElementType.TEXT_AREA
                label = "What will you do today?"
                hint =
                    "For eg: Today I will be wasting most of my time by laughing and gossiping around."
                name = "today"
                maxLength = 3000
            }
        }
    }
    slackClient.sendShowDialog(dialog, triggerId)
}