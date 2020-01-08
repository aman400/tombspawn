package com.tombspawn.skeleton.models.config

import com.google.gson.annotations.SerializedName

class ServerConf(
    @SerializedName("scheme") val scheme: String? = "http",
    @SerializedName("host") val host: String? = null,
    @SerializedName("port") val port: Int? = null,
    @SerializedName("debug") val debug: Boolean? = null
)