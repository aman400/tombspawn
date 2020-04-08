@file:JvmName("CommandLineClient")
package com.tombspawn.skeleton.commandline

import com.tombspawn.base.common.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger("com.tombspawn.skeleton.commandline.CommandLineClient")

@ExperimentalCoroutinesApi
fun CoroutineScope.getCommandExecutor(): SendChannel<Command> {
    return actor(Dispatchers.IO, capacity = Channel.UNLIMITED) {
        consumeEach { command ->
            when (command) {
                is Request -> {
                    try {
                        if (command is Processable) {
                            LOGGER.trace("Pre-processing command")
                            runBlocking {
                                command.onPreProcess()
                            }
                            LOGGER.trace("Pre-processing complete")
                        }
                        LOGGER.debug("Executing: ${command.command}")
                        val strings = command.command.split(Regex("\\s+"))
                        val process = ProcessBuilder(strings)
                            .directory(command.workingDir)
                            .redirectOutput(ProcessBuilder.Redirect.PIPE)
                            .redirectError(ProcessBuilder.Redirect.PIPE)
                            .start()
                        val successResponseBuilder = StringBuilder()
                        launch(Dispatchers.Default) {
                            try {
                                process.inputStream.bufferedReader().forEachLine {
                                    LOGGER.info(it)
                                    successResponseBuilder.appendln(it)
                                }
                            } catch (exception: Exception) {
                                LOGGER.error("Unable to attach stream", exception)
                            }
                        }
                        val errorResponseBuilder = StringBuilder()
                        launch(Dispatchers.Default) {
                            try {
                                process.errorStream.bufferedReader().forEachLine {
                                    LOGGER.info(it)
                                    errorResponseBuilder.appendln(it)
                                }
                            } catch (exception: Exception) {
                                LOGGER.error("Unable to attach error stream", exception)
                            }
                        }
                        process.apply {
                            // Let the process run for timeout
                            LOGGER.info("Starting process with timeout of ${command.timeoutAmount} minutes")

                            waitFor(command.timeoutAmount, command.timeoutUnit)
                            LOGGER.warn("Killing the process")

                            // Destroy the process after timeout.
                            // This won't execute if process executed with success or failure before timeout
                            destroyForcibly()
                            waitFor()
                        }
                        val exitValue = process.exitValue()
                        LOGGER.debug("Process killed with exit value $exitValue")

                        if (exitValue == 0) {
                            LOGGER.info("${command.command} Command successfully executed")
                            if (command is Processable) {
                                LOGGER.trace("Post-processing successful command")
                                runBlocking {
                                    command.onPostProcess(Success(successResponseBuilder.toString()))
                                }
                            }
                            command.listener?.complete(Success(successResponseBuilder.toString()))
                        } else {
                            LOGGER.info("${command.command} Command failed to execute")
                            if (command is Processable) {
                                LOGGER.trace("Post-processing failed command")
                                runBlocking {
                                    command.onPostProcess(Failure(errorResponseBuilder.toString()))
                                }
                            }
                            command.listener?.complete(Failure(errorResponseBuilder.toString()))
                        }
                    } catch (exception: Exception) {
                        LOGGER.error("Exception executing command", exception)
                        if (command is Processable) {
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