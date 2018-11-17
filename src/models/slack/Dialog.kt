package com.ramukaka.models.slack

import com.google.gson.annotations.SerializedName

class Dialog(
    @SerializedName("callback_id")
    val callbackId: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("submit_label")
    val submitLabel: String,
    @SerializedName("notify_on_cancel")
    val notifyOnCancel: Boolean = false,
    @SerializedName("state")
    val state: String? = null,
    @SerializedName("elements")
    val elements: List<Element>? = null
)