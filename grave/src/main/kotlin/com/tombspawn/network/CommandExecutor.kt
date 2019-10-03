package com.tombspawn.network

import com.tombspawn.models.CommandResponse
import com.tombspawn.models.Reference

interface CommandExecutor {

    suspend fun fetchAllBranches(): List<Reference>?
    suspend fun fetchProductFlavours(): List<String>?
    suspend fun fetchBuildVariants(): List<String>?
    suspend fun pullCode(selectedBranch: String): CommandResponse
    suspend fun generateApp(parameters: MutableMap<String, String>?,
                            uploadDirPath: String, APKPrefix: String): CommandResponse
}