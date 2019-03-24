package com.ramukaka.models.slack

import com.google.gson.annotations.SerializedName
import models.slack.UserProfile

class SlackProfileResponse(@SerializedName("ok") val success: Boolean,
                           @SerializedName("profile") val user: UserProfile
)