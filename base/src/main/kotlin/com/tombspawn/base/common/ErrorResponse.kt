package com.tombspawn.base.common

import com.google.gson.annotations.SerializedName

data class ErrorResponse constructor(@SerializedName("details") val details: String? = null,
                    @SerializedName("error_code") val errorCode: String? = null): CallResponse {

    companion object {
        const val ERR_CLEAN_FAILURE = "ERR_001"
    }
}