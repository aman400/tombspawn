package com.tombspawn.skeleton.gradle

import com.tombspawn.base.common.CommandResponse
import com.tombspawn.skeleton.models.Reference
import kotlinx.coroutines.CompletableDeferred
import javax.inject.Inject

class GradleService @Inject constructor(private val gradleExecutor: GradleExecutor) {
    suspend fun fetchBranches(): List<Reference>? {
        return gradleExecutor.fetchAllBranches()
    }

    suspend fun fetchProductFlavours(): List<String>? {
        return gradleExecutor.fetchProductFlavours()
    }

    suspend fun fetchBuildVariants(): List<String>? {
        return gradleExecutor.fetchBuildVariants()
    }

    suspend fun pullCode(selectedBranch: String): CommandResponse {
        return gradleExecutor.pullCode(selectedBranch)
    }

    suspend fun cleanCode(): CommandResponse {
        return gradleExecutor.cleanCode()
    }

    suspend fun executeTask(task: String, parameters: MutableMap<String, String>?, onPreProcess: suspend () -> Boolean,
                            onPostProcess: suspend (response : CommandResponse) -> Boolean): CompletableDeferred<CommandResponse> {
        return gradleExecutor.executeTask(task, parameters, onPreProcess, onPostProcess)
    }

    suspend fun generateApp(parameters: MutableMap<String, String>?,
                            uploadDirPath: String, APKPrefix: String, onPreProcess: (suspend () -> Boolean)): CommandResponse {
        return gradleExecutor.generateApp(parameters, uploadDirPath, APKPrefix, onPreProcess)
    }
}