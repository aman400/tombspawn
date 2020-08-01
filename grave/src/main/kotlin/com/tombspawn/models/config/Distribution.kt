package com.tombspawn.models.config

import com.google.gson.annotations.SerializedName

data class Distribution (
    @SerializedName("host") val host: String,
    @SerializedName("port") val port: Int,
    @SerializedName("testers") val testers: Boolean
)