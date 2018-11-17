package com.ramukaka.models.slack

import com.google.gson.annotations.SerializedName

class SlackResponse(@SerializedName("ok") val success: Boolean,
                    @SerializedName("error") val error: String)