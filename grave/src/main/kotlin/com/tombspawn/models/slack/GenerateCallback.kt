package com.tombspawn.models.slack

import com.google.gson.annotations.SerializedName

class GenerateCallback constructor(
    @SerializedName("action") var action: Action = Action.UNSUBSCRIBE,
    @SerializedName("") var data: MutableMap<String, String>? = null
) : ActionCallback() {
    operator fun Pair<String, String>.unaryPlus() {
        if(data == null) {
            data = mutableMapOf()
        }
        data?.put(this.first, this.second)
    }

    operator fun MutableMap<String, String>?.invoke(block: MutableMap<String, String>?.() -> Unit) {
        block()
    }

    enum class Action {
        @SerializedName("unsubscribe")
        UNSUBSCRIBE,
        @SerializedName("generate")
        GENERATE,
        @SerializedName("do_nothing")
        DO_NOTHING
    }
}

fun generateCallback(block: GenerateCallback.() -> Unit) = GenerateCallback().apply(block)