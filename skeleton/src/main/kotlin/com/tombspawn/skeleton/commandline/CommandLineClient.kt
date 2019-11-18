package com.tombspawn.skeleton.commandline

import com.tombspawn.base.common.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import java.io.IOException

fun CoroutineScope.getCommandExecutor(): SendChannel<Command> {
    return actor(Dispatchers.IO, capacity = Channel.UNLIMITED) {
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
                        if (exitValue == 0) {
                            command.listener?.complete(Success(response))
                        } else {
                            command.listener?.complete(Failure(errorText))
                        }
                    } catch (exception: IOException) {
                        exception.printStackTrace()
                        command.listener?.complete(Failure(null, exception))
                    }
                }
            }
        }
    }
}