package com.tombspawn.models

import com.google.gson.annotations.SerializedName
import com.tombspawn.git.CredentialProvider
import com.tombspawn.models.config.App
import com.tombspawn.models.config.Common

data class AppContainerRequest(@SerializedName("app") val app: App,
                               @SerializedName("common") val common: Common,
                               @SerializedName("git") val credentialProvider: CredentialProvider)