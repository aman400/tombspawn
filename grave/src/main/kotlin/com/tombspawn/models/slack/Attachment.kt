package com.tombspawn.models.slack

import com.google.gson.annotations.SerializedName

class Attachment(
    @SerializedName("callback_id") var callbackId: String? = null,
    @SerializedName("fallback") var fallback: String? = null,
    @SerializedName("text") var text: String? = null,
    @SerializedName("id") var id: Int? = 0,
    @SerializedName("color") var color: String? = null,
    @SerializedName("actions") var actions: MutableList<Action>? = null,
    @SerializedName("attachment_type") var attachmentType: AttachmentType? = null
) {
    operator fun Action.unaryPlus() {
        if(actions == null) {
            actions = mutableListOf()
        }
        actions?.add(this)
    }

    operator fun MutableList<Action>.unaryPlus() {
        if(actions == null) {
            actions = this
        } else {
            actions?.addAll(this)
        }
    }

    operator fun MutableList<Action>?.invoke(function: MutableList<Action>?.() -> Unit) {
        function()
    }

    enum class AttachmentType(val value: String) {
        @SerializedName("default")
        DEFAULT("default")
    }
}

fun attachment(block: Attachment.() -> Unit) = Attachment().apply(block)