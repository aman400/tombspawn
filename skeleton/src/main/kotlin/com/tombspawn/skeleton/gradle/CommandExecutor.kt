package com.tombspawn.skeleton.gradle

import com.tombspawn.base.common.CommandResponse
import com.tombspawn.skeleton.models.Reference
import kotlinx.coroutines.CompletableDeferred

interface CommandExecutor {

    suspend fun fetchAllBranches(): List<Reference>?
    suspend fun fetchProductFlavours(): List<String>?
    suspend fun fetchBuildVariants(): List<String>?
    suspend fun pullCode(selectedBranch: String): CommandResponse
    suspend fun cleanCode(): CommandResponse
    suspend fun executeTask(
        task: String, parameters: MutableMap<String, String>?, onPreProcess: suspend () -> Boolean,
        onPostProcess: suspend (response: CommandResponse) -> Boolean
    ): CompletableDeferred<CommandResponse>

    suspend fun generateApp(
        parameters: MutableMap<String, String>?, uploadDirPath: String, APKPrefix: String,
        onPreProcess: (suspend () -> Boolean)
    ): CommandResponse
}