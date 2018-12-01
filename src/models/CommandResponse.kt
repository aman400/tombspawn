package com.ramukaka.models

sealed class CommandResponse

data class Success(val data: String?) : CommandResponse()

data class Failure(val error: String? = null, val throwable: Throwable? = null) : CommandResponse()