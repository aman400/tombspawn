package com.tombspawn.skeleton.gradle

import com.tombspawn.base.common.CommandResponse
import com.tombspawn.skeleton.models.Reference
import kotlinx.coroutines.CompletableDeferred
import javax.inject.Inject

class GradleService @Inject constructor(private val gradleExecutor: GradleExecutor) {
    suspend fun cleanCode(): CommandResponse {
        return gradleExecutor.cleanCode()
    }

    suspend fun executeTask(task: String, parameters: MutableMap<String, String>?, onPreProcess: suspend () -> Boolean,
                            onPostProcess: suspend (response : CommandResponse) -> Boolean): CompletableDeferred<CommandResponse> {
        return gradleExecutor.executeTask(task, parameters, onPreProcess, onPostProcess)
    }
}