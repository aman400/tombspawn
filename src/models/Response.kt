package com.ramukaka.models

import com.google.gson.annotations.SerializedName

data class Response(@SerializedName("ok") val delivered: Boolean,
                    @SerializedName("message") val message: String? = null,
                    @SerializedName("error") val error: String? = null
)