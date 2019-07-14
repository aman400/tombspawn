package com.ramukaka.auth.models

import com.google.gson.annotations.SerializedName

data class AuthRequest(@SerializedName("request_id") val requestId: String)