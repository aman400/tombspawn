package com.tombspawn.models.slack

import com.google.gson.annotations.SerializedName

class Channel(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("is_member") val member: Boolean?
)