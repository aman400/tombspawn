package com.tombspawn.skeleton.gradle

import com.tombspawn.base.common.CommandResponse
import kotlinx.coroutines.CompletableDeferred

interface CommandExecutor {

    suspend fun initApplication(): Boolean
    suspend fun cleanCode(task: String): CommandResponse
    suspend fun executeTask(
        task: String, parameters: MutableMap<String, String>?, timeout: Long,
        onPreProcess: suspend () -> Boolean, onPostProcess: suspend (response: CommandResponse) -> Boolean,
        executionDir: String? = null
    ): CompletableDeferred<CommandResponse>
}