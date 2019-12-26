package com.tombspawn.models

import com.google.gson.annotations.SerializedName
import com.tombspawn.git.CredentialProvider
import com.tombspawn.models.config.App
import com.tombspawn.models.config.Common
import com.tombspawn.models.config.ServerConf

data class AppContainerRequest constructor(
    @SerializedName("server") val serverConf: ServerConf,
    @SerializedName("app") val app: App,
    @SerializedName("common") val common: Common,
    @SerializedName("git") val credentialProvider: CredentialProvider,
    @SerializedName("init_callback_uri") val callbackUri: String
)