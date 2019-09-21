package com.ramukaka.models

import com.google.gson.annotations.SerializedName

data class CallResponse(@SerializedName("ok") val delivered: Boolean,
                    @SerializedName("message") val message: String? = null,
                    @SerializedName("error") val error: String? = null
)