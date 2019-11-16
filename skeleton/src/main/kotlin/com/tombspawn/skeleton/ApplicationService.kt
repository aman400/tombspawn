package com.tombspawn.skeleton

import com.google.gson.JsonObject
import com.tombspawn.base.common.*
import com.tombspawn.base.extensions.await
import com.tombspawn.base.network.MultiPartContent
import com.tombspawn.skeleton.di.qualifiers.FileUploadDir
import com.tombspawn.skeleton.di.qualifiers.UploadAppClient
import com.tombspawn.skeleton.git.GitService
import com.tombspawn.skeleton.gradle.GradleService
import com.tombspawn.skeleton.models.RefType
import com.tombspawn.skeleton.models.Reference
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.http.HttpMethod
import io.ktor.util.error
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject

class ApplicationService @Inject constructor(
    private val gitService: GitService,
    private val gradleService: GradleService,
    @UploadAppClient
    private val uploadHttpClient: HttpClient,
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

    suspend fun generateApp(parameters: MutableMap<String, String>?,
                            callbackUri: String?, appPrefix: String?) = coroutineScope {

        val apkPrefix = "${appPrefix?.let {
            "$it-"
        } ?: ""}${System.currentTimeMillis()}"

        when (val response = gradleService.generateApp(parameters, fileUploadDir, apkPrefix)) {
            is Success -> {
                callbackUri?.let { url ->
                    val tempDirectory = File(fileUploadDir)
                    if (tempDirectory.exists()) {
                        tempDirectory.listFiles { _, name ->
                            name.contains(apkPrefix, true)
                        }?.firstOrNull()?.let { file ->
                            if (file.exists()) {
                                val responseData = uploadHttpClient.call(url) {
                                    method = HttpMethod.Post
                                    body = MultiPartContent.build {
                                        add("title", apkPrefix)
                                        add("file", file.readBytes(), filename = file.name)
                                    }
                                }.await<JsonObject>()

                                when (responseData) {
                                    is CallSuccess -> {
                                        LOGGER.debug("Uploaded successfully")
                                    }
                                    is CallFailure -> {
                                        responseData.throwable?.let {
                                            LOGGER.error(it)
                                        }
                                    }
                                    is CallError -> {
                                        responseData.throwable?.let {
                                            LOGGER.error(it)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } ?: run {
                    LOGGER.error("Callback uri is null")
                }
            }
            is Failure -> {
                response.throwable?.printStackTrace()
            }
            null -> {
            }
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger("com.tombspawn.skeleton.ApplicationService")
    }
}