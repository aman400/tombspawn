package com.ramukaka.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ErrorResponse(@Expose @SerializedName("response_type") var responseType: String = "in_channel",
                         @Expose @SerializedName("text") var response: String)