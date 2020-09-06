package com.tombspawn.models.config

import com.google.gson.annotations.SerializedName

data class Slack(
    @SerializedName("token") val botToken: String,
    @SerializedName("auth_token") val authToken: String,
    @SerializedName("client_id") val clientId: String,
    @SerializedName("secret") val secret: String
)