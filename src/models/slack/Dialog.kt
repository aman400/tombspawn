package com.ramukaka.models.slack

import com.google.gson.annotations.SerializedName

class Dialog(
    // Callback
    @SerializedName("callback_id")
    var callbackId: String = "",
    @SerializedName("title")
    var title: String = "",
    @SerializedName("submit_label")
    var submitLabel: String = "",
    @SerializedName("notify_on_cancel")
    var notifyOnCancel: Boolean = false,
    // Send this data back
    @SerializedName("state")
    var state: String? = null,
    @SerializedName("elements")
    var elements: MutableList<Element>? = mutableListOf()
) {
    operator fun Element.unaryPlus() {
        if(elements == null) {
            elements = mutableListOf()
        }
        elements?.add(this)
    }

    operator fun MutableList<Element>.unaryPlus() {
        if(elements == null) {
            elements = mutableListOf()
        }
        elements?.addAll(this)
    }

    operator fun MutableList<Element>?.invoke(function: () -> Unit) {
        function()
    }
}

fun dialog(block: Dialog.() -> Unit) = Dialog().apply(block)