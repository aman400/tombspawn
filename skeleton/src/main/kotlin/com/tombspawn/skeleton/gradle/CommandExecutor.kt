package com.tombspawn.skeleton.gradle

import com.tombspawn.base.common.CommandResponse
import com.tombspawn.skeleton.models.Reference

interface CommandExecutor {

    suspend fun fetchAllBranches(): List<Reference>?
    suspend fun fetchProductFlavours(): List<String>?
    suspend fun fetchBuildVariants(): List<String>?
    suspend fun pullCode(selectedBranch: String): CommandResponse
    suspend fun generateApp(parameters: MutableMap<String, String>?,
                            uploadDirPath: String, APKPrefix: String,
                            onPreProcess: (suspend () -> Boolean)): CommandResponse
}