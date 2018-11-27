package com.ramukaka.models.slack

import com.google.gson.annotations.SerializedName

data class SlackMessage(
    @SerializedName("text") var message: String?,
    @SerializedName("username") var username: String?,
    @SerializedName("bot_id") var botId: String?,
    @SerializedName("type") var type: String?,
    @SerializedName("subtype") var subType: String?,
    @SerializedName("ts") var timestamp: String?,
    @SerializedName("attachments") var attachments: List<Attachment>?
)