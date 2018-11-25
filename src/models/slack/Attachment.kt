package com.ramukaka.models.slack

import com.google.gson.annotations.SerializedName
import models.slack.Action

class Attachment(
    @SerializedName("callback_id") val callbackId: String?,
    @SerializedName("fallback") val fallback: String?,
    @SerializedName("text") val text: String?,
    @SerializedName("id") val id: Int,
    @SerializedName("color") val color: String?,
    @SerializedName("actions") val actions: MutableList<Action>?,
    @SerializedName("attachment_type") val attachmentType: AttachmentType? = null
) {
    enum class AttachmentType(val value: String) {
        @SerializedName("default")
        DEFAULT("default")
    }
}