package com.ramukaka.models.slack

import com.google.gson.annotations.SerializedName
import models.slack.Action
import models.slack.Event
import models.slack.SlackUser

data class SlackEvent(
    @SerializedName("token") val token: String?,
    @SerializedName("challenge") val challenge: String?,
    @SerializedName("type") val type: Event.EventType?,
    @SerializedName("user") val user: SlackUser?,
    @SerializedName("channel") val channel: Channel?,
    @SerializedName("event_ts") val timestamp: Long?,
    @SerializedName("team_id") val teamId: String?,
    @SerializedName("api_app_id") val appTeamId: String?,
    @SerializedName("event") val event: Event?,
    @SerializedName("authed_users") val users: MutableList<String>?,
    @SerializedName("actions") val actions: MutableList<Action>?,
    @SerializedName("callback_id") val callbackId: String?,
    @SerializedName("response_url") val responseUrl: String?,
    @SerializedName("trigger_id") val triggerId: String?,
    @SerializedName("submission") val dialogResponse: Map<String, String?>?,
    @SerializedName("original_message") val originalMessage: SlackMessage?,
    @SerializedName("state") val echoed: String?
)