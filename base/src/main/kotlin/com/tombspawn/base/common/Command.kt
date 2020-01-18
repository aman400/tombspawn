package com.tombspawn.base.common

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

sealed class Command

open class Request constructor(
    val command: String,
    val workingDir: File = File("."),
    val timeoutAmount: Long = 15,
    val timeoutUnit: TimeUnit = TimeUnit.MINUTES,
    val id: String = UUID.randomUUID().toString(),
    val listener: CompletableDeferred<CommandResponse>? = null
) : Command()

interface Processable {
    suspend fun onPreProcess(): Boolean {
        return true
    }
    suspend fun onPostProcess(response: CommandResponse): Boolean {
        return true
    }
}

class GenerateAppCommand constructor(command: String, workingDir: File = File("."),
                                     timeoutAmount: Long = 15, timeoutUnit: TimeUnit = TimeUnit.MINUTES,
                                     id: String = UUID.randomUUID().toString(),
                                     listener: CompletableDeferred<CommandResponse>? = null,
                                     val preProcess: (suspend () -> Boolean)):
    Request(command, workingDir, timeoutAmount, timeoutUnit, id, listener), Processable {
    override suspend fun onPreProcess(): Boolean {
        return this@GenerateAppCommand.preProcess()
    }
}

class ExecuteTaskCommand constructor(command: String, workingDir: File = File("."),
                                     timeoutAmount: Long = 15, timeoutUnit: TimeUnit = TimeUnit.MINUTES,
                                     id: String = UUID.randomUUID().toString(),
                                     listener: CompletableDeferred<CommandResponse>? = null,
                                     val preProcess: (suspend () -> Boolean),
                                     val postProcess: (suspend (commandResponse: CommandResponse) -> Boolean)) :
    Request(command, workingDir, timeoutAmount, timeoutUnit, id, listener), Processable {
    override suspend fun onPreProcess(): Boolean {
        return this.preProcess()
    }

    override suspend fun onPostProcess(response: CommandResponse): Boolean {
        return this.postProcess(response)
    }
}

sealed class CommandResponse : Command()

data class Success(val data: String?) : CommandResponse()

data class Failure(val error: String? = null, val throwable: Throwable? = null) : CommandResponse()