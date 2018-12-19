package com.ramukaka.extensions

import com.ramukaka.models.Command
import com.ramukaka.models.Failure
import com.ramukaka.models.Success
import com.ramukaka.network.RetrofitResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import java.io.IOException


fun CoroutineScope.gradleCommandExecutor(
    receiveChannel: ReceiveChannel<Command>
) = produce {
    for (command in receiveChannel) {
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

            send(if (exitValue == 0) Success(response) else Failure(errorText))
        } catch (exception: IOException) {
            exception.printStackTrace()
            send(Failure(throwable = exception))
        }
    }
}

suspend fun <T> retryCall(
    times: Int = 0,
    initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000,    // 1 second
    factor: Double = 2.0,
    block: suspend () -> RetrofitResponse<T>
): RetrofitResponse<T> {
    var currentDelay = initialDelay
    repeat(times) {
        when (block()) {
            is com.ramukaka.network.Success -> {
                return block()
            }
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtLeast(maxDelay)
    }
    return block() // last attempt
}