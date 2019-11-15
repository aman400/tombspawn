package com.tombspawn.skeleton.models.config

import com.google.gson.annotations.SerializedName

class ServerConf(
    @SerializedName("host") val host: String? = null,
    @SerializedName("port") val port: Int? = null
)