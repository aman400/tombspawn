package com.ramukaka.models.slack

import com.google.gson.annotations.SerializedName
import models.slack.Action
import models.slack.Event
import models.slack.SlackUser

data class SlackEvent(@field:SerializedName("token") val token: String?,
                      @field:SerializedName("challenge") val challenge: String?,
                      @field:SerializedName("type") val type: String?,
                      @field:SerializedName("user") val user: SlackUser?,
                      @field:SerializedName("channel") val channel: Channel?,
                      @field:SerializedName("event_ts") val timestamp: Long?,
                      @field:SerializedName("team_id") val teamId: String?,
                      @field:SerializedName("api_app_id") val appTeamId: String?,
                      @field:SerializedName("event") val event: Event?,
                      @field:SerializedName("authed_users") val users: MutableList<String>?,
                      @field:SerializedName("actions") val actions: MutableList<Action>?,
                      @field:SerializedName("callback_id") val callbackId: String?,
                      @field:SerializedName("response_url") val responseUrl: String?
)