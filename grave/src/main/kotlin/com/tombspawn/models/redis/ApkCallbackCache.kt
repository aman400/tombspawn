package com.tombspawn.models.redis

import com.google.gson.annotations.SerializedName

data class ApkCallbackCache(@SerializedName("callback_id") val callbackId: String,
                            @SerializedName("response_url") val responseUrl: String? = null,
                            @SerializedName("channel_id") val channelId: String? = null)