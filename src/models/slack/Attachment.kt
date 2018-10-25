package com.ramukaka.models.slack

import com.google.gson.annotations.SerializedName
import models.slack.Action

class Attachment(
    @field:SerializedName("callback_id") val callbackId: String?,
    @field:SerializedName("fallback") val fallback: String?,
    @field:SerializedName("text") val text: String?,
    @field:SerializedName("id") val id: Int,
    @field:SerializedName("color") val color: String?,
    @field:SerializedName("actions") val actions: MutableList<Action>?
)