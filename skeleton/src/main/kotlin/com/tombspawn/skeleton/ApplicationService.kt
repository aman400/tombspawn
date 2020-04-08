package com.tombspawn.skeleton

import com.tombspawn.base.Ref
import com.tombspawn.base.common.*
import com.tombspawn.skeleton.app.AppClient
import com.tombspawn.skeleton.di.qualifiers.InitCallbackUri
import com.tombspawn.skeleton.git.GitService
import com.tombspawn.skeleton.gradle.CommandService
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
    private val gradleService: CommandService,
    private val appClient: AppClient,
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
            if (gitService.clone() && gradleService.runInitScripts()) {
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
            } else {
                throw IllegalStateException("Unable to initialize application")
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
        return gradleService.cleanCode("clean")
    }

    @ExperimentalStdlibApi
    suspend fun generateApplication(
        parameters: MutableMap<String, String> = mutableMapOf(),
        onSuccess: ((file: File, extraData: Map<String, String?>) -> Unit),
        onFailure: ((message: String?, throwable: Throwable?) -> Unit)
    ) = coroutineScope {
        // find the selected gradle task
        app.gradleTasks?.firstOrNull {
            it.id == parameters[SlackConstants.TYPE_SELECT_BUILD_TYPE]
        }?.let { gradleTask ->
            val branch = parameters[SlackConstants.TYPE_SELECT_BRANCH]
            val lastTask = gradleTask.tasks.size - 1
            val jobs: MutableList<Job> = mutableListOf()
            // Run all the gradle subtasks defined for a given taskId
            gradleTask.tasks.forEachIndexed { index: Int, task: String ->
                jobs.add(gradleService.executeTask(task, parameters, gradleTask.timeout, {
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
                    // Perform all the exit operations for a given task
                    if (index == lastTask) {
                        when (response) {
                            is Success -> {
                                val paths = Paths.get(app.appDir!!)
                                val outputDir = try {
                                    paths.resolve(gradleTask.outputDir).toFile()
                                } catch (exception: Exception) {
                                    LOGGER.error("Unable to resolve directory paths", exception)
                                    null
                                }
                                // find the apk in the output directory
                                if (outputDir?.exists() == true && outputDir.isDirectory) {
                                    FileUtils.iterateFiles(outputDir, arrayOf("apk"), true)
                                        .takeIf {
                                            it.hasNext()
                                        }?.next()?.let { file ->
                                            if (file.exists()) {
                                                // get last commit logs from git.
                                                val params = buildMap<String, String?> {
                                                    gitService.fetchLogs()?.also {
                                                        put(CommonConstants.COMMIT_MESSAGE, it.shortMessage)
                                                        put(CommonConstants.COMMIT_ID, it.id.name)
                                                        put(
                                                            CommonConstants.COMMIT_AUTHOR,
                                                            it.authorIdent?.let { author ->
                                                                "${author.name}<${author.emailAddress}> ${HUMAN_DATE_FORMAT.format(
                                                                    author.getWhen()
                                                                )}"
                                                            })
                                                    }
                                                }
                                                LOGGER.info("APK File generated")
                                                onSuccess(file, params)
                                                true
                                            } else {
                                                LOGGER.error("Apk file cannot be located in output directory")
                                                onFailure("Apk file cannot be located in output directory", null)
                                                false
                                            }
                                        } ?: run {
                                        LOGGER.error("Apk not found in output directory")
                                        onFailure("Apk not found in output directory", null)
                                        false
                                    }
                                } else {
                                    // Output directory
                                    LOGGER.error("Output directory not found")
                                    onFailure("Output directory not found", null)
                                    false
                                }
                            }
                            is Failure -> {
                                LOGGER.error("Failed to execute task ${response.error}", response.throwable)
                                onFailure("Failed to execute task ${response.error ?: response.throwable?.message}", response.throwable)
                                false
                            }
                        }
                    } else {
                        true
                    }
                }, gradleTask.executionDir))
            }
            // Await for completion of all jobs
            jobs.forEach {
                it.join()
            }
        } ?: run {
            onFailure("No gradle task specified", null)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger("com.tombspawn.skeleton.ApplicationService")
    }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO
}
