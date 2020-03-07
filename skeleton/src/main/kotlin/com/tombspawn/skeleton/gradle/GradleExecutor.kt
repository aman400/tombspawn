package com.tombspawn.skeleton.gradle

import com.tombspawn.base.common.*
import com.tombspawn.skeleton.di.qualifiers.AppDir
import com.tombspawn.skeleton.di.qualifiers.GradlePath
import com.tombspawn.skeleton.git.CredentialProvider
import com.tombspawn.skeleton.models.RefType
import com.tombspawn.skeleton.models.Reference
import com.tombspawn.skeleton.utils.Constants
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import javax.inject.Inject

class GradleExecutor @Inject constructor(
    @AppDir
    private val appDir: String,
    @GradlePath
    private val gradlePath: String,
    private val requestExecutor: SendChannel<@JvmSuppressWildcards Command>,
    private val credentialProvider: CredentialProvider
) : CommandExecutor {

    private val LOGGER = LoggerFactory.getLogger("com.tombspawn.skeleton.gradle.GradleExecutor")

    override suspend fun fetchAllBranches(): List<Reference>? {
        val executableCommand =
            "$gradlePath fetchRemoteBranches -P${Constants.Common.ARG_OUTPUT_SEPARATOR}=${Constants.Common.OUTPUT_SEPARATOR}"
        val id = UUID.randomUUID().toString()
        val request = Request(executableCommand, File(appDir), id = id, listener = CompletableDeferred())
        requestExecutor.send(request)
        when (val response = request.listener!!.await()) {
            is Success -> {
                LOGGER.debug("Branches fetched")
                response.data?.let {
                    val parsedResponse = it.split(Constants.Common.OUTPUT_SEPARATOR)
                    if (parsedResponse.size >= 2) {
                        return parsedResponse[1]
                            .split("\n")
                            .filter { item -> item.isNotEmpty() }
                            .map {
                                Reference(
                                    it.substringAfter("#tag-", it),
                                    if (it.startsWith("#tag-")) RefType.TAG else RefType.BRANCH
                                )
                            }
                    }
                }
            }
            is Failure -> {
                LOGGER.error("Unable to fetch branches", response.throwable)
            }
        }
        return null
    }

    override suspend fun fetchProductFlavours(): List<String>? {
        val executableCommand =
            "$gradlePath getProductFlavours -P${Constants.Common.ARG_OUTPUT_SEPARATOR}=${Constants.Common.OUTPUT_SEPARATOR}"
        val id = UUID.randomUUID().toString()
        val request = Request(executableCommand, File(appDir), id = id, listener = CompletableDeferred())
        requestExecutor.send(request)
        when (val response = request.listener!!.await()) {
            is Success -> {
                LOGGER.debug("Product flavours fetched")
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
                LOGGER.error("Unable to fetch product flavours", response.throwable)
            }
        }
        return null
    }

    override suspend fun fetchBuildVariants(): List<String>? {
        val executableCommand =
            "$gradlePath getBuildVariants -P${Constants.Common.ARG_OUTPUT_SEPARATOR}=${Constants.Common.OUTPUT_SEPARATOR}"
        val id = UUID.randomUUID().toString()
        val request = Request(executableCommand, File(appDir), id = id, listener = CompletableDeferred())
        requestExecutor.send(request)
        when (val response = request.listener!!.await()) {
            is Success -> {
                LOGGER.debug("Build variants fetched")
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
                LOGGER.error("Unable to fetch build variants", response.throwable)
            }
        }
        return null
    }

    override suspend fun cleanCode(): CommandResponse {
        val cleanCommand = "$gradlePath clean"
        val executionDirectory = File(appDir)
        val id = UUID.randomUUID().toString()
        val request = Request(cleanCommand, executionDirectory, id = id, listener = CompletableDeferred())
        requestExecutor.send(request)
        return request.listener!!.await()
    }

    override suspend fun pullCode(selectedBranch: String): CommandResponse {
        val pullCodeCommand =
            "$gradlePath pullCode ${selectedBranch.let { "-P${SlackConstants.TYPE_SELECT_BRANCH}=$it" }}"

        val executionDirectory = File(appDir)
        val id = UUID.randomUUID().toString()
        val request = Request(pullCodeCommand, executionDirectory, id = id, listener = CompletableDeferred())
        requestExecutor.send(request)
        return request.listener!!.await()
    }

    override suspend fun executeTask(
        task: String,
        parameters: MutableMap<String, String>?,
        onPreProcess: suspend () -> Boolean,
        onPostProcess: suspend (response: CommandResponse) -> Boolean
    ): CompletableDeferred<CommandResponse> {
        val executionDirectory = File(appDir)
        var executableCommand = "$gradlePath $task"

        parameters?.filter {
            it.key != SlackConstants.TYPE_SELECT_APP_PREFIX && it.key != SlackConstants.TYPE_ADDITIONAL_PARAMS
                    && it.key != SlackConstants.TYPE_SELECT_BRANCH && it.key != SlackConstants.TYPE_SELECT_BUILD_TYPE
        }?.forEach { key, value ->
            executableCommand += " -P$key=$value"
        }

        val buildId = UUID.randomUUID().toString()
        val request = ExecuteTaskCommand(
            executableCommand, executionDirectory,
            id = buildId, listener = CompletableDeferred(), preProcess = onPreProcess, postProcess = onPostProcess
        )
        requestExecutor.send(request)
        return request.listener!!
    }

    override suspend fun generateApp(
        parameters: MutableMap<String, String>?,
        uploadDirPath: String, APKPrefix: String,
        onPreProcess: (suspend () -> Boolean)
    ): CommandResponse {
        val executionDirectory = File(appDir)
        var executableCommand =
            "$gradlePath assembleWithArgs -PFILE_PATH=$uploadDirPath -P${SlackConstants.TYPE_SELECT_APP_PREFIX}=$APKPrefix"

        parameters?.filter {
            it.key != SlackConstants.TYPE_SELECT_APP_PREFIX
                    && it.key != SlackConstants.TYPE_ADDITIONAL_PARAMS
                    && it.key != SlackConstants.TYPE_SELECT_BRANCH
        }?.forEach { key, value ->
            executableCommand += " -P$key=$value"
        }

        executableCommand += " -Pgradlebot.git.username=${credentialProvider.username}"

        val buildId = UUID.randomUUID().toString()
        val request = GenerateAppCommand(
            executableCommand, executionDirectory,
            id = buildId, listener = CompletableDeferred(), preProcess = onPreProcess
        )
        requestExecutor.send(request)
        return request.listener!!.await()
    }
}