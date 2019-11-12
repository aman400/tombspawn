package com.tombspawn.app

import com.tombspawn.base.common.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import java.io.IOException

class CommandLineService constructor(private val coroutineScope: CoroutineScope) {

    fun commandExecutor(responseListeners: MutableMap<String, CompletableDeferred<CommandResponse>>): SendChannel<Command> {
        return coroutineScope.actor(Dispatchers.IO, capacity = Channel.UNLIMITED) {
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
    }
}