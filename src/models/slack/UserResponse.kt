package com.ramukaka.models.slack

import com.google.gson.annotations.SerializedName
import models.slack.IMListData
import models.slack.SlackUser

data class UserResponse(@SerializedName("ok") val successful: Boolean? = false,
                        @SerializedName("response_metadata") val responseMetadata: IMListData.ResponseMetadata? = null,
                        @SerializedName("members") val members: List<SlackUser>? = null)