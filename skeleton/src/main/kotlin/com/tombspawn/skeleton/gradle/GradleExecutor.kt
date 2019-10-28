package com.tombspawn.skeleton.gradle

import com.tombspawn.base.common.*
import com.tombspawn.skeleton.git.CredentialProvider
import com.tombspawn.skeleton.models.*
import com.tombspawn.skeleton.utils.Constants
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import java.io.File
import java.util.*

class GradleExecutor constructor(
    private val appDir: String,
    private val gradlePath: String,
    private val responseListeners: MutableMap<String, CompletableDeferred<CommandResponse>>,
    private val requestExecutor: SendChannel<Command>,
    private val credentialProvider: CredentialProvider
): CommandExecutor {
    override suspend fun fetchAllBranches(): List<Reference>? {
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
                            .map { Reference(it.substringAfter("#tag-", it),
                                if(it.startsWith("#tag-")) RefType.TAG else RefType.BRANCH) }
                    }
                }
            }
            is Failure -> {
                response.throwable?.printStackTrace()
            }
        }
        return null
    }

    override suspend fun fetchProductFlavours(): List<String>? {
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
            is Failure -> {
                response.throwable?.printStackTrace()
            }
        }
        return null
    }

    override suspend fun fetchBuildVariants(): List<String>? {
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
            is Failure -> {
                response.throwable?.printStackTrace()
            }
        }
        return null
    }

    override suspend fun pullCode(selectedBranch: String): CommandResponse {
        val pullCodeCommand = "$gradlePath pullCode ${selectedBranch.let { "-P${Constants.Apis.TYPE_SELECT_BRANCH}=$it" }}"

        val executionDirectory = File(appDir)
        val id = UUID.randomUUID().toString()
        requestExecutor.send(Request(pullCodeCommand, executionDirectory, id = id))
        val responseListener = CompletableDeferred<CommandResponse>()
        responseListeners[id] = responseListener
        return responseListener.await()
    }

    override suspend fun generateApp(parameters: MutableMap<String, String>?,
                                     uploadDirPath: String, APKPrefix: String): CommandResponse {
        parameters?.remove(Constants.Apis.TYPE_ADDITIONAL_PARAMS)
        val executionDirectory = File(appDir)

        parameters?.remove(Constants.Apis.TYPE_SELECT_APP_PREFIX)

        var executableCommand =
            "$gradlePath assembleWithArgs -PFILE_PATH=$uploadDirPath -P${Constants.Apis.TYPE_SELECT_APP_PREFIX}=$APKPrefix"

        parameters?.forEach { key, value ->
            executableCommand += " -P$key=$value"
        }

        executableCommand += " -Pgradlebot.git.username=${credentialProvider.username}"

        val buildId = UUID.randomUUID().toString()
        requestExecutor.send(Request(executableCommand, executionDirectory, id = buildId))
        val buildApkResponseListener = CompletableDeferred<CommandResponse>()
        responseListeners[buildId] = buildApkResponseListener
        return buildApkResponseListener.await()
    }
}