package com.tombspawn.skeleton

import com.tombspawn.base.common.*
import com.tombspawn.skeleton.app.AppClient
import com.tombspawn.skeleton.di.qualifiers.FileUploadDir
import com.tombspawn.skeleton.git.GitService
import com.tombspawn.skeleton.gradle.GradleService
import com.tombspawn.skeleton.models.RefType
import com.tombspawn.skeleton.models.Reference
import io.ktor.util.error
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject

class ApplicationService @Inject constructor(
    private val gitService: GitService,
    private val gradleService: GradleService,
    private val appClient: AppClient,
    @FileUploadDir
    private val fileUploadDir: String
) {

    fun init() {
        clone()
    }

    private fun clone() {
        gitService.clone()
    }

    suspend fun fetchRemoteBranches() {
        gitService.fetchRemoteBranches().await()
    }

    suspend fun getReferences(branchLimit: Int = -1, tagLimit: Int = -1): MutableList<Reference> {
        return mutableListOf<Reference>().apply {
            addAll(getBranches(branchLimit))
            addAll(getTags(tagLimit))
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

    suspend fun fetchBranches(): List<Reference>? {
        return gradleService.fetchBranches()
    }

    suspend fun fetchProductFlavours(): List<String>? {
        return gradleService.fetchProductFlavours()
    }

    suspend fun fetchBuildVariants(): List<String>? {
        return gradleService.fetchBuildVariants()
    }

    suspend fun pullCode(selectedBranch: String): CommandResponse {
        return gradleService.pullCode(selectedBranch)
    }

    suspend fun generateApp(parameters: MutableMap<String, String>?, successCallbackUri: String?,
                            failureCallbackUri: String?, apkPrefix: String) = coroutineScope {
        val branch = parameters?.get(SlackConstants.TYPE_SELECT_BRANCH)

        when (val response = gradleService.generateApp(parameters, fileUploadDir, apkPrefix) {
            branch?.let {
                // Checkout to given branch before app generation
                checkoutBranch(it)
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
}