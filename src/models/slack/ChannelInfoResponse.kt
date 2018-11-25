package com.ramukaka.models.slack

import com.google.gson.annotations.SerializedName

class ChannelInfoResponse(@SerializedName("ok") val success: Boolean,
                          @SerializedName("channel") val channel: Channel)