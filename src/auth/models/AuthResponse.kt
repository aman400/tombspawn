package com.ramukaka.auth.models

import com.google.gson.annotations.SerializedName

data class AuthResponse(@SerializedName("auth_token") val token: String)