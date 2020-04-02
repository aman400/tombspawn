package com.tombspawn.skeleton.gradle

import com.tombspawn.base.common.CommandResponse
import com.tombspawn.skeleton.models.Reference
import kotlinx.coroutines.CompletableDeferred

interface CommandExecutor {

    suspend fun cleanCode(task: String): CommandResponse
    suspend fun executeTask(
        task: String, parameters: MutableMap<String, String>?, onPreProcess: suspend () -> Boolean,
        onPostProcess: suspend (response: CommandResponse) -> Boolean
    ): CompletableDeferred<CommandResponse>
}