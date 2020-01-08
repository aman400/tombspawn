package com.tombspawn.base.common

import com.google.gson.annotations.SerializedName

data class ErrorResponse constructor(@SerializedName("details") val details: String? = null,
                    @SerializedName("error_code") val errorCode: String? = null)