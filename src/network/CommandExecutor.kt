package com.ramukaka.network

import com.ramukaka.models.CommandResponse
import com.ramukaka.models.Reference

interface CommandExecutor {

    suspend fun fetchAllBranches(): List<Reference>?
    suspend fun fetchProductFlavours(): List<String>?
    suspend fun fetchBuildVariants(): List<String>?
    suspend fun pullCode(selectedBranch: String): CommandResponse
    suspend fun generateApp(parameters: MutableMap<String, String>?,
                            uploadDirPath: String, APKPrefix: String): CommandResponse
}