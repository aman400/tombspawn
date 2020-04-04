package com.tombspawn.skeleton.gradle

import com.tombspawn.base.common.*
import com.tombspawn.skeleton.di.qualifiers.AppDir
import com.tombspawn.skeleton.di.qualifiers.GradlePath
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import javax.inject.Inject

class GradleExecutor @Inject constructor(
    @AppDir
    private val appDir: String,
    @GradlePath
    private val gradlePath: String,
    private val requestExecutor: SendChannel<@JvmSuppressWildcards Command>
) : CommandExecutor {

    private val LOGGER = LoggerFactory.getLogger("com.tombspawn.skeleton.gradle.GradleExecutor")

    override suspend fun cleanCode(task: String): CommandResponse {
        LOGGER.info("Cleaning code directory")
        val cleanCommand = "$gradlePath $task"
        val executionDirectory = File(appDir)
        val id = UUID.randomUUID().toString()
        val request = Request(cleanCommand, executionDirectory, id = id, listener = CompletableDeferred())
        requestExecutor.send(request)
        return request.listener!!.await()
    }

    override suspend fun executeTask(
        task: String,
        parameters: MutableMap<String, String>?,
        timeout: Long,
        onPreProcess: suspend () -> Boolean,
        onPostProcess: suspend (response: CommandResponse) -> Boolean,
        executionDir: String?
    ): CompletableDeferred<CommandResponse> {
        val executionDirectory = File(executionDir ?: appDir)
        var executableCommand = "$gradlePath $task"

        parameters?.filter {
            it.key != SlackConstants.TYPE_SELECT_APP_PREFIX && it.key != SlackConstants.TYPE_ADDITIONAL_PARAMS
                    && it.key != SlackConstants.TYPE_SELECT_BRANCH && it.key != SlackConstants.TYPE_SELECT_BUILD_TYPE
        }?.forEach { key, value ->
            executableCommand += " -P$key=$value"
        }

        val buildId = UUID.randomUUID().toString()
        val request = ExecuteTaskCommand(
            executableCommand, executionDirectory,
            timeoutAmount = timeout,
            id = buildId, listener = CompletableDeferred(), preProcess = onPreProcess, postProcess = onPostProcess
        )
        requestExecutor.send(request)
        return request.listener!!
    }
}