package com.tombspawn.models.slack

import com.google.gson.annotations.SerializedName

class CallbackMessage<T> constructor(
    @SerializedName("action") var action: Action = Action.POSITIVE,
    @SerializedName("data") var data: T? = null
) : ActionCallback() {

    operator fun MutableMap<String, String>?.invoke(block: MutableMap<String, String>?.() -> Unit) {
        block()
    }

    enum class Action {
        @SerializedName("unsubscribe")
        POSITIVE,
        @SerializedName("generate")
        NEGATIVE,
        @SerializedName("none")
        NONE
    }
}

fun <T> callbackMessage(block: CallbackMessage<T>.() -> Unit) = CallbackMessage<T>().apply(block)