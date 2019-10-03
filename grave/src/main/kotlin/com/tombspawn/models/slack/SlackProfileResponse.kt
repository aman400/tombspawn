package com.tombspawn.models.slack

import com.google.gson.annotations.SerializedName

class SlackProfileResponse(@SerializedName("ok") val success: Boolean,
                           @SerializedName("profile") val user: UserProfile
)