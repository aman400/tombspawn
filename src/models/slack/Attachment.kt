package com.ramukaka.models.slack

import com.google.gson.annotations.SerializedName
import models.slack.Action

class Attachment(
    @SerializedName("callback_id") val callbackId: String? = null,
    @SerializedName("fallback") val fallback: String? = null,
    @SerializedName("text") val text: String? = null,
    @SerializedName("id") val id: Int? = 0,
    @SerializedName("color") val color: String? = null,
    @SerializedName("actions") val actions: MutableList<Action>? = null,
    @SerializedName("attachment_type") val attachmentType: AttachmentType? = null
) {
    enum class AttachmentType(val value: String) {
        @SerializedName("default")
        DEFAULT("default")
    }
}