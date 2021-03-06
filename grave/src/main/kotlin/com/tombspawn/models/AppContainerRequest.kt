package com.tombspawn.models

import com.google.gson.annotations.SerializedName
import com.tombspawn.models.config.App
import com.tombspawn.models.config.Common
import com.tombspawn.models.config.ServerConf

data class AppContainerRequest constructor(
    @SerializedName("ktor") val ktorConfig: KtorConfig,
    @SerializedName("app") val app: App,
    @SerializedName("common") val common: Common,
    @SerializedName("init_callback_uri") val callbackUri: String
) {
    data class KtorConfig(@SerializedName("deployment") val serverConf: ServerConf)
}