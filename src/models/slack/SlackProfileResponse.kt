package com.ramukaka.models.slack

import com.google.gson.annotations.SerializedName
import models.slack.UserProfile

class SlackProfileResponse(@field:SerializedName("ok") val success: Boolean,
                           @field:SerializedName("profile") val user: UserProfile
)