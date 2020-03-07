@file:JvmName("CommandLineClient")
package com.tombspawn.skeleton.commandline

import com.tombspawn.base.common.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.io.IOException

private val LOGGER = LoggerFactory.getLogger("com.tombspawn.skeleton.commandline.CommandLineClient")

fun CoroutineScope.getCommandExecutor(): SendChannel<Command> {
    return actor(Dispatchers.IO, capacity = Channel.UNLIMITED) {
        consumeEach { command ->
            when (command) {
                is Request -> {
                    try {
                        if(command is Processable) {
                            LOGGER.trace("Pre-processing command")
                            runBlocking {
                                command.onPreProcess()
                            }
                            LOGGER.trace("Pre-processing complete")
                        }
                        LOGGER.debug("Executing: ${command.command}")
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
                            LOGGER.info("${command.command} Command successfully executed")
                            if(command is Processable) {
                                LOGGER.trace("Post-processing successful command")
                                runBlocking {
                                    command.onPostProcess(Success(response))
                                }
                            }
                            command.listener?.complete(Success(response))
                        } else {
                            LOGGER.info("${command.command} Command failed to execute")
                            if(command is Processable) {
                                LOGGER.trace("Post-processing failed command")
                                runBlocking {
                                    command.onPostProcess(Failure(errorText))
                                }
                            }
                            command.listener?.complete(Failure(errorText))
                        }
                    } catch (exception: IOException) {
                        LOGGER.error("Exception executing command", exception)
                        if(command is Processable) {
                            LOGGER.trace("Post-processing failed command")
                            runBlocking {
                                command.onPostProcess(Failure("Command failed with exception", exception))
                            }
                        }
                        command.listener?.complete(Failure(null, exception))
                    }
                }
            }
        }
    }
}