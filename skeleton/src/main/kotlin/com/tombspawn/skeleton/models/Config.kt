package com.tombspawn.skeleton.models

import com.google.gson.annotations.SerializedName
import com.tombspawn.skeleton.git.CredentialProvider
import com.tombspawn.skeleton.models.config.CommonConfig

class Config(@SerializedName("app") val app: App,
             @SerializedName("command") val command: CommonConfig,
             @SerializedName("git") val credentialProvider: CredentialProvider
)