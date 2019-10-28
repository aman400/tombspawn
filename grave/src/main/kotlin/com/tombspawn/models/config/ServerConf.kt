package com.tombspawn.models.config

import com.google.gson.annotations.SerializedName

data class ServerConf(
    @SerializedName("host") val host: String? = null,
    @SerializedName("port") val port: Int? = null
)