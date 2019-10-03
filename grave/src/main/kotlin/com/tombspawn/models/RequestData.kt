package com.tombspawn.models

import com.google.gson.annotations.SerializedName

data class RequestData(@SerializedName("response_type") var responseType: String = "in_channel",
                       @SerializedName("text") var response: String)