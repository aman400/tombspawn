package com.tombspawn.skeleton

import com.tombspawn.base.common.*
import com.tombspawn.skeleton.app.AppClient
import com.tombspawn.skeleton.di.qualifiers.FileUploadDir
import com.tombspawn.skeleton.di.qualifiers.InitCallbackUri
import com.tombspawn.skeleton.git.GitService
import com.tombspawn.skeleton.gradle.GradleService
import com.tombspawn.skeleton.models.RefType
import com.tombspawn.skeleton.models.Reference
import io.ktor.util.error
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class ApplicationService @Inject constructor(
    private val gitService: GitService,
    private val gradleService: GradleService,
    private val appClient: AppClient,
    @FileUploadDir
    private val fileUploadDir: String,
    @InitCallbackUri
    private val initCallbackUri: String
): CoroutineScope {

    private val job = Job()

    fun init() {
        clone()
    }

    fun clear() {
        job.cancelChildren()
    }

    private fun clone() {
        launch(Dispatchers.IO) {
            if(gitService.clone()) {
                LOGGER.info("Making api call to $initCallbackUri")
                when(val response = appClient.initComplete(initCallbackUri, success = true)) {
                    is CallSuccess -> {
                        LOGGER.info(response.data.toString())
                    }
                    is CallFailure -> {
                        LOGGER.info("Call failure ${response.errorBody}", response.throwable)
                    }
                    is CallError -> {
                        LOGGER.info("Call Error", response.throwable)
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
            if(branchLimit >= 0) {
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

    suspend fun checkoutBranch(branch: String): Boolean {
        return gitService.checkout(branch).await()
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

    suspend fun pullCode(selectedBranch: String): CommandResponse {
        return gradleService.pullCode(selectedBranch)
    }

    suspend fun generateApp(parameters: MutableMap<String, String>?, successCallbackUri: String?,
                            failureCallbackUri: String?, apkPrefix: String) = coroutineScope {
        val branch = parameters?.get(SlackConstants.TYPE_SELECT_BRANCH)

        when (val response = gradleService.generateApp(parameters, fileUploadDir, apkPrefix) {
            branch?.let {
                // Stash uncommited code
                gitService.stashCode().await()?.also {
                    LOGGER.info("Stashed older code ${it.fullMessage}")
                } ?: run {
                    LOGGER.debug("Nothing to stash")
                }
                // Clear stash list
                gitService.clearStash().await()?.also {
                    LOGGER.info("Cleared stash with object id ${it.name}")
                } ?: run {
                    LOGGER.debug("No stash list to clear")
                }
                // Checkout to given branch before app generation
                checkoutBranch(it)
                true
            } ?: false
        }) {
            is Success -> {
                val tempDirectory = File(fileUploadDir)
                if (tempDirectory.exists()) {
                    tempDirectory.listFiles { _, name ->
                        name.contains(apkPrefix, true)
                    }?.firstOrNull()?.let { file ->
                        if (file.exists()) {
                            successCallbackUri?.let { url ->
                                when (val responseData = appClient.uploadFile(url, apkPrefix, file, parameters)) {
                                    is CallSuccess -> {
                                        file.delete()
                                        LOGGER.debug("Uploaded successfully")
                                    }
                                    is CallFailure -> {
                                        file.delete()
                                        responseData.throwable?.let {
                                            LOGGER.error(it)
                                        }
                                        failureCallbackUri?.let { errorUrl ->
                                            appClient.reportFailure(errorUrl, ErrorResponse(responseData.errorBody ?:
                                            "Something went wrong. Unable to upload file"))
                                        }
                                    }
                                    is CallError -> {
                                        file.delete()
                                        responseData.throwable?.let {
                                            LOGGER.error(it)
                                        }
                                        failureCallbackUri?.let { errorUrl ->
                                            appClient.reportFailure(errorUrl, ErrorResponse(
                                                responseData.throwable?.message
                                                ?: "Something went wrong. Unable to upload file"))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    failureCallbackUri?.let { errorUrl ->
                        appClient.reportFailure(errorUrl, ErrorResponse(
                            "File not found"))
                    }
                }
            }
            is Failure -> {
                failureCallbackUri?.let { errorUrl ->
                    LOGGER.error(response.error)
                    appClient.reportFailure(errorUrl, ErrorResponse(
                        response.error ?: "File not found"))
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