package com.ramukaka.models.slack

import com.google.gson.annotations.SerializedName

class GenerateCallback(@SerializedName("generate") val generate: Boolean,
                       @SerializedName("data") val data: Map<String, String>? = null) : ActionCallback()