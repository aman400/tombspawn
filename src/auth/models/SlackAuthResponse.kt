package com.ramukaka.auth.models

import com.google.gson.annotations.SerializedName
import models.slack.SlackUser

data class SlackAuthResponse(@SerializedName("ok") val successful: Boolean? = false,
                             @SerializedName("access_token") val accessToken: String? = null,
                             @SerializedName("scope") val scope: String? = null,
                             @SerializedName("user") val user: SlackUser? = null) {

}