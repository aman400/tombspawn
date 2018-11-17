package com.ramukaka.models.slack

import com.google.gson.annotations.SerializedName

class Command(
    @SerializedName("token")
    val token: String?,
    @SerializedName("team_id")
    val teamId: String?,
    @SerializedName("team_domain")
    val teamDomain: String?,
    @SerializedName("channel_id")
    val channelId: String?,
    @SerializedName("channel_name")
    val channel_name: String?,
    @SerializedName("user_id")
    val userId: String?,
    @SerializedName("user_name")
    val user_name: String?,
    @SerializedName("command")
    val command: String?,
    @SerializedName("text")
    val text: String?,
    @SerializedName("response_url")
    val responseUrl: String?,
    @SerializedName("trigger_id")
    val triggerId: String?
) {
}