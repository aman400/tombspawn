package com.tombspawn.skeleton.gradle

import com.tombspawn.base.common.CommandResponse
import com.tombspawn.skeleton.models.Reference
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

    suspend fun generateApp(parameters: MutableMap<String, String>?,
                            uploadDirPath: String, APKPrefix: String): CommandResponse {
        return gradleExecutor.generateApp(parameters, uploadDirPath, APKPrefix)
    }
}