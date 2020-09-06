package com.tombspawn.session.models

import com.google.gson.annotations.SerializedName

data class LoginSession(
    @SerializedName("email")
    var email: String,
    @SerializedName("token")
    var token: String
)