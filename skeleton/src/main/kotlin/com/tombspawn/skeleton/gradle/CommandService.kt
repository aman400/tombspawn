package com.tombspawn.skeleton.gradle

import com.tombspawn.base.common.CommandResponse
import kotlinx.coroutines.CompletableDeferred
import javax.inject.Inject

class CommandService @Inject constructor(private val commandExecutor: CommandExecutor) {

    suspend fun runInitScripts(): Boolean {
        return commandExecutor.initApplication()
    }

    suspend fun cleanCode(cleanTask: String): CommandResponse {
        return commandExecutor.cleanCode(cleanTask)
    }

    suspend fun executeTask(
        task: String, parameters: MutableMap<String, String>?, timeout: Long,
        onPreProcess: suspend () -> Boolean, onPostProcess: suspend (response: CommandResponse) -> Boolean,
        executionDir: String? = null
    ): CompletableDeferred<CommandResponse> {
        return commandExecutor.executeTask(task, parameters, timeout, onPreProcess, onPostProcess, executionDir)
    }
}