package com.ramukaka.extensions

import com.ramukaka.models.*
import com.ramukaka.network.Response
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import java.io.IOException


fun CoroutineScope.commandExecutor(responseListeners: MutableMap<String, CompletableDeferred<CommandResponse>>): SendChannel<Command> =
    actor(Dispatchers.IO, capacity = Channel.UNLIMITED) {
        consumeEach { command ->
            when (command) {
                is Request -> {
                    try {
                        val strings = command.command.split(Regex("\\s+"))
                        val builder = ProcessBuilder(strings)
                            .directory(command.workingDir)
                            .redirectOutput(ProcessBuilder.Redirect.PIPE)
                            .redirectError(ProcessBuilder.Redirect.PIPE)
                            .start()
                        val response = builder.inputStream.bufferedReader().readText()
                        val errorText = builder.errorStream.bufferedReader().readText()
                        builder.apply {
                            waitFor(command.timeoutAmount, command.timeoutUnit)
                        }
                        val exitValue = builder.exitValue()

                        val listener = responseListeners[command.id]

                        if (exitValue == 0) {
                            listener?.complete(Success(response))
                        } else {
                            listener?.complete(Failure(errorText))
                        }
                        responseListeners.remove(command.id)
                    } catch (exception: IOException) {
                        exception.printStackTrace()
                        responseListeners[command.id]?.complete(Failure(null, exception))
                        responseListeners.remove(command.id)
                    }
                }
            }
        }
    }

suspend fun <T> retryCall(
    times: Int = 0,
    initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000,    // 1 second
    factor: Double = 2.0,
    block: suspend () -> Response<T>
): Response<T> {
    var currentDelay = initialDelay
    repeat(times) {
        val data = block()
        when (data) {
            is com.ramukaka.network.Success -> {
                return data
            }
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtLeast(maxDelay)
    }
    return block() // last attempt
}