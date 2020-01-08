package com.tombspawn.models.slack

import com.google.gson.annotations.SerializedName

class GenerateCallback(
    @SerializedName("generate") var generate: Boolean = false,
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
}

fun generateCallback(block: GenerateCallback.() -> Unit) = GenerateCallback().apply(block)