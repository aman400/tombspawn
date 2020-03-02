package com.tombspawn.skeleton

import com.tombspawn.base.Ref
import com.tombspawn.base.common.*
import com.tombspawn.skeleton.app.AppClient
import com.tombspawn.skeleton.di.qualifiers.FileUploadDir
import com.tombspawn.skeleton.di.qualifiers.InitCallbackUri
import com.tombspawn.skeleton.exception.AppNotGeneratedException
import com.tombspawn.skeleton.git.GitService
import com.tombspawn.skeleton.gradle.GradleService
import com.tombspawn.skeleton.models.App
import kotlinx.coroutines.*
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

    suspend fun getReferences(branchLimit: Int = -1, tagLimit: Int = -1): List<Ref> {
        // fetch all remote references from server including branches and tags
        gitService.fetchRemoteBranches()
        return mutableListOf<Ref>().apply {
            addAll(getBranches(branchLimit))
            addAll(getTags(tagLimit))
        }
    }

    private suspend fun getBranches(branchLimit: Int): List<Ref> {
        return gitService.getBranches().let {
            if (branchLimit >= 0) {
                it.take(branchLimit)
            } else {
                it
            }
        }.map {
            Ref.newBuilder().setName(it).setType(Ref.Type.BRANCH).build()
        }
    }

    private suspend fun getTags(tagLimit: Int): List<Ref> {
        return gitService.getTags().let {
            if (tagLimit >= 0) {
                it.take(tagLimit)
            } else {
                it
            }
        }.map {
            Ref.newBuilder()
                .setName(it)
                .setType(Ref.Type.TAG)
                .build()
        }
    }

    suspend fun cleanCode(): CommandResponse {
        return gradleService.cleanCode()
    }

    suspend fun generateApplication(
        parameters: MutableMap<String, String> = mutableMapOf(),
        onSuccess: ((file: File) -> Unit),
        onFailure: ((throwable: Throwable) -> Unit)
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
                            gitService.clean().let {
                                LOGGER.info(it.joinToString(", "))
                            }
                            // Reset branch to point to head of tracked branch
                            gitService.resetBranch().let { ref ->
                                LOGGER.info("Reset head to ${ref.name} ${ref.objectId}")
                            }
                            gitService.fetchRemoteBranches()
                            // Checkout to given branch before app generation
                            gitService.checkout(it)
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
                                            gitService.fetchLogs()?.let {
                                                parameters[CommonConstants.COMMIT_MESSAGE] = it.shortMessage
                                                parameters[CommonConstants.COMMIT_ID] = it.id.name
                                                it.authorIdent?.let { author ->
                                                    parameters[CommonConstants.COMMIT_AUTHOR] =
                                                        "${author.name}<${author.emailAddress}> ${HUMAN_DATE_FORMAT.format(
                                                            author.getWhen()
                                                        )}"
                                                }
                                            }

                                            LOGGER.info("APK File generated")
                                            onSuccess(file)
                                            true
                                        } else {
                                            LOGGER.error("File cannot be located in directory")
                                            onFailure(AppNotGeneratedException("File cannot be located in directory"))
                                            false
                                        }
                                    } ?: run {
                                        LOGGER.error("Apk not found")
                                        onFailure(AppNotGeneratedException("Apk not found"))
                                        false
                                    }
                                } else {
                                    LOGGER.error("Directory not found")
                                    onFailure(AppNotGeneratedException("Directory not found"))
                                    false
                                }
                            }
                            is Failure -> {
                                LOGGER.error("Failed to execute task ${response.error}", response.throwable)
                                onFailure(AppNotGeneratedException("Failed to execute task ${response.error}"))
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
            onFailure(AppNotGeneratedException("No gradle task specified"))
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger("com.tombspawn.skeleton.ApplicationService")
    }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO
}
