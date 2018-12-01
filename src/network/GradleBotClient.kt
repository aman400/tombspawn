package com.ramukaka.network

import com.ramukaka.extensions.execute
import com.ramukaka.models.Success
import com.ramukaka.utils.Constants
import java.io.File

class GradleBotClient(private val gradlePath: String, private val appDir: String) {
    suspend fun fetchAllBranches(): List<String>? {
        val executableCommand =
            "$gradlePath fetchRemoteBranches -P${Constants.Common.ARG_OUTPUT_SEPARATOR}=${Constants.Common.OUTPUT_SEPARATOR}"
        val response = executableCommand.execute(File(appDir)).await()
        if (response is Success) {
            response.data?.let {
                val parsedResponse = it.split(Constants.Common.OUTPUT_SEPARATOR)
                if (parsedResponse.size >= 2) {
                    return parsedResponse[1]
                        .split("\n")
                        .filter { item -> item.isNotEmpty() }
                }
            }
        }
        return null
    }

    suspend fun fetchProductFlavours(): List<String>? {
        val executableCommand =
            "$gradlePath getProductFlavours -P${Constants.Common.ARG_OUTPUT_SEPARATOR}=${Constants.Common.OUTPUT_SEPARATOR}"
        val response = executableCommand.execute(File(appDir)).await()
        if (response is Success) {
            response.data?.let {
                val parsedResponse = it.split(Constants.Common.OUTPUT_SEPARATOR)
                if (parsedResponse.size >= 2) {
                    return parsedResponse[1]
                        .split("\n")
                        .filter { item -> item.isNotEmpty() }
                }
            }
        }
        return null
    }

    suspend fun fetchBuildVariants(): List<String>? {
        val executableCommand =
            "$gradlePath getBuildVariants -P${Constants.Common.ARG_OUTPUT_SEPARATOR}=${Constants.Common.OUTPUT_SEPARATOR}"
        val response = executableCommand.execute(File(appDir)).await()
        if (response is Success) {
            response.data?.let {
                val parsedResponse = it.split(Constants.Common.OUTPUT_SEPARATOR)
                if (parsedResponse.size >= 2) {
                    return parsedResponse[1]
                        .split("\n")
                        .filter { item -> item.isNotEmpty() }
                }
            }
        }
        return null
    }
}