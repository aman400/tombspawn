package com.tombspawn.models.config

import com.google.gson.annotations.SerializedName

data class Db(
    @SerializedName("url") val url: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("password") val password: String?
)