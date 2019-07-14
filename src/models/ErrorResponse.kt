package com.ramukaka.models

import com.google.gson.annotations.SerializedName

class ErrorResponse(@SerializedName("error_code") val errorCode: String,
                    @SerializedName("details") val details: String)