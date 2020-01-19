package com.tombspawn.skeleton

import com.tombspawn.base.common.*
import com.tombspawn.base.extensions.asString
import com.tombspawn.skeleton.app.AppClient
import com.tombspawn.skeleton.di.qualifiers.FileUploadDir
import com.tombspawn.skeleton.di.qualifiers.InitCallbackUri
import com.tombspawn.skeleton.git.GitService
import com.tombspawn.skeleton.gradle.GradleService
import com.tombspawn.skeleton.models.App
import com.tombspawn.skeleton.models.RefType
import com.tombspawn.skeleton.models.Reference
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.asFlow
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


class ApplicationService @Inject constructor(
    private val gitService: GitService,
    private val gradleService: GradleService,
    private val appClient: AppClient,
    @FileUploadDir
    private val fileUploadDir: String,
    @InitCallbackUri
    private val initCallbackUri: String,
    private val app: App
) : CoroutineScope {

    private val HUMAN_DATE_FORMAT by lazy {
        SimpleDateFormat("h:mm a, EEE, MMM d, ''yy").apply {
            timeZone = TimeZone.getTimeZone("GMT+5:30")
        }
    }

    private val job = Job()

    fun init() {
        clone()
    }

    fun clear() {
        job.cancelChildren()
    }

    private fun clone() {
        launch(Dispatchers.IO) {
            if (gitService.clone()) {
                LOGGER.info("Making api call to $initCallbackUri")
                when (val response = appClient.initComplete(initCallbackUri, success = true)) {
                    is CallSuccess -> {
                        LOGGER.info(response.data.toString())
                    }
                    is CallFailure -> {
                        LOGGER.info("Call failure $initCallbackUri", response.throwable)
                    }
                    is ServerFailure -> {
                        LOGGER.info("Call failure $initCallbackUri", response.throwable)
                    }
                    is CallError -> {
                        LOGGER.info("Call Error $initCallbackUri", response.throwable)
                    }
                }.exhaustive
            }
        }
    }

    suspend fun getReferences(callbackUri: String, branchLimit: Int = -1, tagLimit: Int = -1) {
        // fetch all remote references from server including branches and tags
        gitService.fetchRemoteBranches().await()
        mutableListOf<Reference>().apply {
            addAll(getBranches(branchLimit))
            addAll(getTags(tagLimit))
        }.let {
            appClient.sendReferences(callbackUri, it)
        }
    }

    suspend fun getBranches(branchLimit: Int): List<Reference> {
        return gitService.getBranches().await().let {
            if (branchLimit >= 0) {
                it.take(branchLimit)
            } else {
                it
            }
        }.map {
            Reference(it, RefType.BRANCH)
        }
    }

    suspend fun getTags(tagLimit: Int): List<Reference> {
        return gitService.getTags().await().let {
            if (tagLimit >= 0) {
                it.take(tagLimit)
            } else {
                it
            }
        }.map {
            Reference(it, RefType.TAG)
        }
    }

    suspend fun checkoutBranch(branch: String): Deferred<Boolean> {
        return gitService.checkout(branch)
    }

    suspend fun fetchProductFlavours(callbackUri: String) {
        gradleService.fetchProductFlavours()?.let {
            appClient.sendFlavours(callbackUri, it)
        } ?: run {
            appClient.sendFlavours(callbackUri, listOf())
        }
    }

    suspend fun fetchBuildVariants(callbackUri: String) {
        gradleService.fetchBuildVariants()?.let {
            appClient.sendBuildVariants(callbackUri, it)
        } ?: run {
            appClient.sendBuildVariants(callbackUri, listOf())
        }
    }

    suspend fun cleanCode(callbackUri: String) {
        when (val response = gradleService.cleanCode()) {
            is Success -> {
                appClient.sendCleanCommandResponse(callbackUri, SuccessResponse("Cleaned code"))
            }
            is Failure -> {
                appClient.sendCleanCommandResponse(
                    callbackUri, ErrorResponse(
                        response.error ?: response.throwable.asString(),
                        ErrorResponse.ERR_CLEAN_FAILURE
                    )
                )
            }
        }.exhaustive
    }

    suspend fun pullCode(selectedBranch: String): Deferred<Boolean> {
        return gitService.pullCode(selectedBranch)
    }

    suspend fun generateApplication(
        parameters: MutableMap<String, String> = mutableMapOf(), successCallbackUri: String?,
        failureCallbackUri: String?, apkPrefix: String
    ) = coroutineScope {
        app.gradleTasks?.firstOrNull {
            it.id == parameters[SlackConstants.TYPE_SELECT_BUILD_TYPE]
        }?.let { gradleTask ->
            val branch = parameters[SlackConstants.TYPE_SELECT_BRANCH]
            val lastTask = gradleTask.tasks.size - 1
            val jobs: MutableList<Job> = mutableListOf()
            gradleTask.tasks.forEachIndexed { index: Int, task: String ->
                jobs.add(gradleService.executeTask(task, parameters, {
                    if (index == 0) {
                        branch?.trim()?.let {
                            // Clean git repo to remove untracked files/folders
                            gitService.clean().await().let {
                                LOGGER.info(it.joinToString(", "))
                            }
                            // Reset branch to point to head of tracked branch
                            gitService.resetBranch().await().let { ref ->
                                LOGGER.info("Reset head to ${ref.name} ${ref.objectId}")
                            }
                            gitService.fetchRemoteBranches().await()
                            // Checkout to given branch before app generation
                            checkoutBranch(it).await()
                        } ?: false
                    } else {
                        true
                    }
                }, { response ->
                    if (index == lastTask) {
                        when (response) {
                            is Success -> {
                                val paths = Paths.get(app.dir!!)
                                val outputDir = try {
                                    paths.resolve(gradleTask.outputDir).toFile()
                                } catch (exception: Exception) {
                                    LOGGER.error("Unable to resolve directory paths", exception)
                                    null
                                }
                                if (outputDir?.exists() == true && outputDir.isDirectory) {
                                    FileUtils.iterateFiles(outputDir, arrayOf("apk"), true)
                                        .takeIf {
                                            it.hasNext()
                                        }?.next()?.let { file ->
                                        if (file.exists()) {
                                            gitService.fetchLogs().await()?.let {
                                                parameters[CommonConstants.COMMIT_MESSAGE] = it.shortMessage
                                                parameters[CommonConstants.COMMIT_ID] = it.id.name
                                                it.authorIdent?.let { author ->
                                                    parameters[CommonConstants.COMMIT_AUTHOR] =
                                                        "${author.name}<${author.emailAddress}> ${HUMAN_DATE_FORMAT.format(
                                                            author.getWhen()
                                                        )}"
                                                }
                                            }
                                            successCallbackUri?.let { url ->
                                                when (val responseData =
                                                    appClient.uploadFile(url, apkPrefix, file, parameters)) {
                                                    is CallSuccess -> {
                                                        file.delete()
                                                        LOGGER.debug("Uploaded successfully")
                                                        true
                                                    }
                                                    is CallFailure -> {
                                                        file.delete()
                                                        LOGGER.error("Unable to upload file", responseData.throwable)
                                                        onTaskFailure(failureCallbackUri, responseData.errorBody)
                                                        false
                                                    }
                                                    is ServerFailure -> {
                                                        file.delete()
                                                        LOGGER.error("Unable to upload file", responseData.throwable)
                                                        onTaskFailure(
                                                            failureCallbackUri,
                                                            responseData.throwable?.message
                                                        )
                                                        false
                                                    }
                                                    is CallError -> {
                                                        file.delete()
                                                        LOGGER.error("Unable to upload file", responseData.throwable)
                                                        onTaskFailure(
                                                            failureCallbackUri,
                                                            responseData.throwable?.message
                                                        )
                                                        false
                                                    }
                                                }
                                            } ?: run {
                                                LOGGER.error("Callback uri is null")
                                                onTaskFailure(failureCallbackUri, "Callback uri is null")
                                                false
                                            }
                                        } else {
                                            LOGGER.error("File cannot be located in directory")
                                            onTaskFailure(failureCallbackUri, "File cannot be located in directory")
                                            false
                                        }
                                    } ?: run {
                                        LOGGER.error("Apk not found")
                                        onTaskFailure(failureCallbackUri, "Apk not found")
                                        false
                                    }
                                } else {
                                    LOGGER.error("Directory not found")
                                    onTaskFailure(failureCallbackUri, "Directory not found")
                                    false
                                }
                            }
                            is Failure -> {
                                LOGGER.error("Failed to execute task ${response.error}", response.throwable)
                                onTaskFailure(failureCallbackUri, "Failed to execute task ${response.error}")
                                false
                            }
                        }
                    } else {
                        true
                    }
                }))
            }
            jobs.forEach {
                it.join()
            }
        } ?: run {
            onTaskFailure(failureCallbackUri, "No gradle task specified")
        }
    }

    private suspend fun onTaskFailure(failureUri: String?, error: String?) = coroutineScope {
        failureUri?.let {
            appClient.reportFailure(
                failureUri, ErrorResponse(
                    error ?: "Something went wrong."
                )
            )
        }
    }

    suspend fun generateApp(
        parameters: MutableMap<String, String> = mutableMapOf(), successCallbackUri: String?,
        failureCallbackUri: String?, apkPrefix: String
    ) = coroutineScope {
        val branch = parameters[SlackConstants.TYPE_SELECT_BRANCH]

        when (val response = gradleService.generateApp(parameters, fileUploadDir, apkPrefix) {
            branch?.trim()?.let {
                // Clean git repo to remove untracked files/folders
                gitService.clean().await().let {
                    LOGGER.info(it.joinToString(", "))
                }
                // Reset branch to point to head of tracked branch
                gitService.resetBranch().await().let { ref ->
                    LOGGER.info("Reset head to ${ref.name} ${ref.objectId}")
                }
                gitService.fetchRemoteBranches().await()
                // Checkout to given branch before app generation
                checkoutBranch(it).await()
            } ?: false
        }) {
            is Success -> {
                val tempDirectory = File(fileUploadDir)
                if (tempDirectory.exists()) {
                    tempDirectory.listFiles { _, name ->
                        name.contains(apkPrefix, true)
                    }?.firstOrNull()?.let { file ->
                        if (file.exists()) {
                            gitService.fetchLogs().await()?.let {
                                parameters[CommonConstants.COMMIT_MESSAGE] = it.shortMessage
                                parameters[CommonConstants.COMMIT_ID] = it.id.name
                                it.authorIdent?.let { author ->
                                    parameters[CommonConstants.COMMIT_AUTHOR] =
                                        "${author.name}<${author.emailAddress}> ${HUMAN_DATE_FORMAT.format(author.getWhen())}"
                                }
                            }
                            successCallbackUri?.let { url ->
                                when (val responseData = appClient.uploadFile(url, apkPrefix, file, parameters)) {
                                    is CallSuccess -> {
                                        file.delete()
                                        LOGGER.debug("Uploaded successfully")
                                    }
                                    is CallFailure -> {
                                        file.delete()
                                        LOGGER.error("Unable to upload file", responseData.throwable)
                                        failureCallbackUri?.let { errorUrl ->
                                            appClient.reportFailure(
                                                errorUrl, ErrorResponse(
                                                    responseData.errorBody
                                                        ?: "Something went wrong. Unable to upload file"
                                                )
                                            )
                                        }
                                    }
                                    is ServerFailure -> {
                                        file.delete()
                                        LOGGER.error("Unable to upload file", responseData.throwable)
                                        failureCallbackUri?.let { errorUrl ->
                                            appClient.reportFailure(
                                                errorUrl, ErrorResponse(
                                                    responseData.throwable?.message
                                                        ?: "Something went wrong. Unable to upload file"
                                                )
                                            )
                                        }
                                    }
                                    is CallError -> {
                                        file.delete()
                                        LOGGER.error("Unable to upload file", responseData.throwable)
                                        failureCallbackUri?.let { errorUrl ->
                                            appClient.reportFailure(
                                                errorUrl, ErrorResponse(
                                                    responseData.throwable?.message
                                                        ?: "Something went wrong. Unable to upload file"
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    failureCallbackUri?.let { errorUrl ->
                        appClient.reportFailure(
                            errorUrl, ErrorResponse(
                                "File not found"
                            )
                        )
                    }
                }
            }
            is Failure -> {
                failureCallbackUri?.let { errorUrl ->
                    LOGGER.error(response.error)
                    appClient.reportFailure(
                        errorUrl, ErrorResponse(
                            response.error ?: "File not found"
                        )
                    )
                }
            }
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger("com.tombspawn.skeleton.ApplicationService")
    }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO
}
