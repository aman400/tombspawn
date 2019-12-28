package com.tombspawn.base.common

import com.google.gson.annotations.SerializedName

data class SuccessResponse constructor(@SerializedName("message") val message: String? = null)