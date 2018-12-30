package com.ramukaka.network

import com.ramukaka.models.*
import com.ramukaka.models.Failure
import com.ramukaka.models.Success
import com.ramukaka.utils.Constants
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import java.io.File
import java.util.*

class GradleBotClient(
    private val gradlePath: String,
    private val responseListeners: MutableMap<String, CompletableDeferred<CommandResponse>>,
    private val requestExecutor: SendChannel<Command>
) {
    suspend fun fetchAllBranches(appDir: String): List<String>? {
        val executableCommand =
            "$gradlePath fetchRemoteBranches -P${Constants.Common.ARG_OUTPUT_SEPARATOR}=${Constants.Common.OUTPUT_SEPARATOR}"
        val id = UUID.randomUUID().toString()
        requestExecutor.send(Request(executableCommand, File(appDir), id = id))
        val responseListener = CompletableDeferred<CommandResponse>()
        responseListeners[id] = responseListener
        when (val response = responseListener.await()) {
            is Success -> {
                response.data?.let {
                    val parsedResponse = it.split(Constants.Common.OUTPUT_SEPARATOR)
                    if (parsedResponse.size >= 2) {
                        return parsedResponse[1]
                            .split("\n")
                            .filter { item -> item.isNotEmpty() }
                    }
                }
            }
            is Failure -> {

            }
        }
        return null
    }

    suspend fun fetchProductFlavours(appDir: String): List<String>? {
        val executableCommand =
            "$gradlePath getProductFlavours -P${Constants.Common.ARG_OUTPUT_SEPARATOR}=${Constants.Common.OUTPUT_SEPARATOR}"
        val id = UUID.randomUUID().toString()
        requestExecutor.send(Request(executableCommand, File(appDir), id = id))
        val responseListener = CompletableDeferred<CommandResponse>()
        responseListeners[id] = responseListener
        when (val response = responseListener.await()) {
            is Success -> {
                response.data?.let {
                    val parsedResponse = it.split(Constants.Common.OUTPUT_SEPARATOR)
                    if (parsedResponse.size >= 2) {
                        return parsedResponse[1]
                            .split("\n")
                            .filter { item -> item.isNotEmpty() }
                    }
                }
            }
        }
        return null
    }

    suspend fun fetchBuildVariants(appDir: String): List<String>? {
        val executableCommand =
            "$gradlePath getBuildVariants -P${Constants.Common.ARG_OUTPUT_SEPARATOR}=${Constants.Common.OUTPUT_SEPARATOR}"
        val id = UUID.randomUUID().toString()
        requestExecutor.send(Request(executableCommand, File(appDir), id = id))
        val responseListener = CompletableDeferred<CommandResponse>()
        responseListeners[id] = responseListener
        when (val response = responseListener.await()) {
            is Success -> {
                response.data?.let {
                    val parsedResponse = it.split(Constants.Common.OUTPUT_SEPARATOR)
                    if (parsedResponse.size >= 2) {
                        return parsedResponse[1]
                            .split("\n")
                            .filter { item -> item.isNotEmpty() }
                    }
                }
            }
        }
        return null
    }
}