package com.ramukaka.models

import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

sealed class Command

data class Request(val command: String, val workingDir: File = File("."),
                          val timeoutAmount: Long = 15,
                          val timeoutUnit: TimeUnit = TimeUnit.MINUTES, val id: String = UUID.randomUUID().toString()) : Command()

sealed class CommandResponse: Command()

data class Success(val data: String?) : CommandResponse()

data class Failure(val error: String? = null, val throwable: Throwable? = null) : CommandResponse()