package com.ramukaka.models.slack

import com.google.gson.annotations.SerializedName

data class SlackMessage(
    @SerializedName("text") var message: String? = null,
    @SerializedName("username") var username: String? = null,
    @SerializedName("bot_id") var botId: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("subtype") var subType: String? = null,
    @SerializedName("ts") var timestamp: String? = null,
    @SerializedName("attachments") var attachments: MutableList<Attachment>? = null
) {
    operator fun Attachment.unaryPlus() {
        if (attachments == null) {
            attachments = mutableListOf(this)
        } else {
            attachments?.add(this)
        }
    }

    operator fun MutableList<Attachment>.unaryPlus() {
        if (attachments == null) {
            attachments = mutableListOf()
        }
        attachments?.addAll(this)
    }

    operator fun MutableList<Attachment>?.invoke(function: MutableList<Attachment>?.() -> Unit) {
        function()
    }
}

fun slackMessage(block: SlackMessage.() -> Unit) = SlackMessage().apply(block)