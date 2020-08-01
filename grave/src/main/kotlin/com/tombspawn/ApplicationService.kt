package com.tombspawn

import com.google.common.base.Optional
import com.google.gson.Gson
import com.tombspawn.base.Ref
import com.tombspawn.base.common.SlackConstants
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.base.extensions.toMap
import com.tombspawn.data.CachingService
import com.tombspawn.data.DatabaseService
import com.tombspawn.data.Refs
import com.tombspawn.data.Subscriptions
import com.tombspawn.data.cache.models.ApkCache
import com.tombspawn.di.qualifiers.ApplicationBaseUri
import com.tombspawn.di.qualifiers.Debuggable
import com.tombspawn.di.qualifiers.UploadDir
import com.tombspawn.di.qualifiers.WaitingMessages
import com.tombspawn.distribution.DistributionService
import com.tombspawn.distribution.distribute
import com.tombspawn.docker.DockerService
import com.tombspawn.models.AppResponse
import com.tombspawn.models.Reference
import com.tombspawn.models.bitbucket.BitbucketResponse
import com.tombspawn.models.bitbucket.getData
import com.tombspawn.models.config.App
import com.tombspawn.models.config.Common
import com.tombspawn.models.config.ServerConf
import com.tombspawn.models.config.Slack
import com.tombspawn.models.github.Payload
import com.tombspawn.models.github.RefType
import com.tombspawn.models.slack.*
import com.tombspawn.slackbot.*
import com.tombspawn.utils.Constants
import com.tombspawn.utils.Constants.Common.SKELETON_DEBUG_PORT
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.ktor.http.URLBuilder
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Provider
import kotlin.coroutines.resume

@AppScope
class ApplicationService @Inject constructor(
    internal val slack: Slack,
    internal val common: Common,
    internal val gson: Gson,
    internal val databaseService: DatabaseService,
    internal val apps: List<App>,
    private val dockerService: DockerService,
    internal val slackService: SlackService,
    @UploadDir
    internal val uploadDir: File,
    internal val cachingService: CachingService,
    @Debuggable
    internal val debug: Boolean,
    internal val config: Optional<ServerConf>,
    @ApplicationBaseUri
    internal val baseUri: Provider<URLBuilder>,
    @WaitingMessages
    internal val randomWaitingMessages: Optional<List<String>>,
    internal val distributionService: DistributionService
) {

    fun onTaskCompleted(id: String) {
        dockerService.onTaskCompleted(id)
    }

    suspend fun init() = coroutineScope {
        addApps()
        fetchBotInfo()
        launch(Dispatchers.Default) {
            dockerService.logEvents()
        }
        launch(Dispatchers.Default) {
            dockerService.startChannels()
        }
        launch(Dispatchers.Default) {
            dockerService.startQueueExecution()
        }
        apps.forEachIndexed { index, app ->
            val callbackUri = baseUri.get().path("apps", app.id, "init").build().toString()
            dockerService.createContainer(app, common.basePort + index, SKELETON_DEBUG_PORT + index, callbackUri)
        }

        updateUserData()
    }

    private suspend fun fetchBotInfo() {
        slackService.fetchBotData(slack.botToken)?.let { about ->
            databaseService.addUser(about.id!!, about.name, typeString = Constants.Database.USER_TYPE_BOT)
        }
    }

    private suspend fun addApps() {
        return databaseService.addApps(apps)
    }

    /**
     * Fetch data for first time initialized container.
     *
     * @param appId is the id of the application
     */
    suspend fun fetchAppData(appId: String) {
        apps.firstOrNull {
            appId == it.id
        }?.let {
            withContext(Dispatchers.IO) {
                runBlocking {
                    dockerService.appInitialized(it)
                }
                fetchAndUpdateReferences(it)
            }
        }
    }

    private suspend fun fetchAndUpdateReferences(app: App) = withContext(Dispatchers.IO) {
        addRefs(app.id, fetchReferences(app).map {
            Reference(it.name, RefType.from(it))
        })
        onTaskCompleted(app.id)
    }

    /**
     * Clean the files cache directory
     */
    private suspend fun cleanAppCacheFiles() = coroutineScope {
        launch(Dispatchers.IO) {
            try {
                uploadDir.let {
                    if (it.exists()) {
                        it.deleteRecursively()
                    }
                }
            } catch (exception: Exception) {
                LOGGER.error("Unable to clean cache directory", exception)
            }
        }
    }

    /**
     * Fetch references for the given application
     *
     * @param app is the app for which references need to be fetched
     */
    private suspend fun fetchReferences(app: App): List<Ref> {
        return dockerService.fetchReferences(app)
    }

    /**
     * Run clean command from given application
     *
     * @param app is app container to clean.
     */
    private suspend fun cleanApp(app: App): Boolean {
        return dockerService.cleanApp(app)
    }

    /**
     * Function to add reference(branches/tags) to redis and database
     *
     * @param appId is app to be cached
     * @param refs is the list of references to be cached to redis and database
     */
    private suspend fun addRefs(appId: String, refs: List<Reference>) {
        databaseService.addRefs(refs, appId)
        cachingService.cacheAppReferences(appId, refs)
    }

    private suspend fun updateUserData() = coroutineScope {
        val users = slackService.getSlackUsers(slack.botToken)
        val ims = slackService.getSlackBotImIds(slack.botToken)
        users.forEach { user ->
            val im = ims.firstOrNull { im ->
                im.user == user.id
            }
            im?.let {
                if (im.isUserDeleted == false && user.bot == false && user.id != Constants.Slack.DEFAULT_BOT_ID) {
                    databaseService.addUser(
                        user.id!!,
                        user.profile?.name,
                        user.profile?.email,
                        Constants.Database.USER_TYPE_USER,
                        im.id
                    )
                }
            }
        }
    }

    suspend fun generateApk(
        buildData: MutableMap<String, String>,
        channelId: String,
        appID: String
    ) = coroutineScope {
        // Get the additional parameters
        val additionalParams = buildData[SlackConstants.TYPE_ADDITIONAL_PARAMS]?.trim()
        // Parse extra parameters into map.
        additionalParams?.let {
            it.toMap()?.forEach { key, value ->
                if (!buildData.containsKey(key)) {
                    buildData[key] = value
                }
            }
        }

        // Remove additional params
        buildData.remove(SlackConstants.TYPE_ADDITIONAL_PARAMS)

        // Find the app to be generated
        apps.firstOrNull {
            it.id == appID
        }?.let { app ->
            val distribute: Boolean = buildData[SlackConstants.TYPE_DISTRIBUTION].toBoolean()
            val task = app.gradleTasks?.firstOrNull {
                it.id == buildData[SlackConstants.TYPE_SELECT_BUILD_TYPE]
            }
            val useCache: Boolean = (task?.useCache ?: true) && !distribute
            LOGGER.debug(if(useCache) "Using Cache" else "Skipping cache")
            val cachedFile = if(useCache) {
                 getCachedApk(buildData, app)
            } else {
                null
            }
            if (cachedFile == null) {
                // Generate the application
                launch(Dispatchers.IO) {
                    try {
                        dockerService.generateApp(
                            app,
                            buildData,
                            verify = {
                                if(useCache) {
                                    getCachedApk(buildData, app)
                                } else {
                                    null
                                }
                            }
                        ).takeIf {
                            it.data != null && it.data.isNotEmpty()
                        }?.let { response ->
                            response.params.forEach { (key, value) ->
                                if(!value.isNullOrEmpty()) {
                                    buildData[key] = value
                                }
                            }
                            val appData = response.data!!
                            // Upload apk to slack
                            launch(Dispatchers.IO) {
                                if (useCache) {
                                    val file = File(uploadDir, response.fileName.let {
                                        "${app.id}-${System.nanoTime()}-${it ?: "App.apk"}"
                                    }).also {
                                        it.writeBytes(appData)
                                    }
                                    verifyAndCacheApp(file, app.id, buildData)
                                    LOGGER.debug("Caching ${file.absolutePath} to disk")
                                } else {
                                    LOGGER.debug("Skipping cache for file")
                                }
                            }
                            if(distribute) {
                                distribute(task!!, appData, buildData, channelId, response.fileName ?: "App.apk") {
                                    onTaskCompleted(app.id)
                                }
                            } else {
                                uploadApk(appData, buildData, channelId, response.fileName ?: "App.apk") {
                                    onTaskCompleted(app.id)
                                }
                            }
                        } ?: run {
                            LOGGER.error("File is null")
                            reportFailure(app, channelId, "Unable to generate apk for request: \n${buildData.map {
                                "${it.key} = ${it.value}"
                            }.joinToString("\n")}")
                        }
                    } catch (exception: Exception) {
                        val message = if (exception is StatusRuntimeException) {
                            when (exception.status.code) {
                                Status.Code.UNKNOWN -> {
                                    exception.status.description ?: exception.status.cause?.let {
                                        it.message ?: it.stackTrace.joinToString("\n") { it.toString() }
                                    }
                                }
                                Status.Code.DEADLINE_EXCEEDED -> {
                                    "App generation request timed out after deadline"
                                }
                                else -> {
                                    "Something went wrong. Error: ${exception.status.code.name}.\n${exception.status}"
                                }
                            }
                        } else {
                            exception.stackTrace.joinToString("\n") { it.toString() }
                        }
                        LOGGER.error(message, exception)
                        reportFailure(app, channelId, message)
                    }
                }
                slackService.sendMessage(
                    randomWaitingMessages.orNull()?.shuffled()
                        ?.firstOrNull() ?: "Please wait", channelId, null
                )
            } else {
                uploadApk(cachedFile.data!!, cachedFile.params, channelId, cachedFile.fileName ?: "App.apk")
            }
        }
    }

    private suspend fun getCachedApk(
        buildData: MutableMap<String, String>, app: App
    ): AppResponse? = suspendCancellableCoroutine { continuation ->
        val apkCache = ApkCache(buildData)
        LOGGER.trace("Fetching from cache")
        val cache = buildData[SlackConstants.TYPE_SELECT_BRANCH]?.let {
            cachingService.getApkCache(app.id, it)
        }?.firstOrNull {
            LOGGER.trace("Matching: \n$it\n&\n$apkCache")
            it == apkCache
        }?.let {
            LOGGER.trace("Match found: \n$it")
            Pair(it, it.pathOnDisk?.let { File(it) })
        }

        if (cache?.second?.exists() == true) {
            LOGGER.trace("file exists")
            continuation.resume(AppResponse(cache.second!!.readBytes(), cache.first.params, cache.second!!.name, true))
        } else {
            LOGGER.trace("file not found")
            continuation.resume(null)
        }
    }

    private suspend fun getFilteredReferences(appId: String, vararg toFilter: RefType): List<Reference> {
        return apps.firstOrNull {
            appId == it.id
        }?.let { app ->
            val branchPattern = if (app.gitConfig?.branchConfig?.regex != null) {
                Pattern.compile(app.gitConfig.branchConfig.regex)
            } else null
            val tagPattern = if (app.gitConfig?.tagConfig?.regex != null) {
                Pattern.compile(app.gitConfig.tagConfig.regex)
            } else null

            // Limit the list to 100. Slack limitation.
            getReferences(app.id)?.filter {
                if(toFilter.isEmpty() || it.type in toFilter) {
                    if (it.type == RefType.TAG) {
                        tagPattern?.matcher(it.name)?.matches() ?: true
                    } else {
                        branchPattern?.matcher(it.name)?.matches() ?: true
                    }
                } else {
                    false
                }
            }.orEmpty()
        }.orEmpty()
    }

    suspend fun showGenerateApkDialog(appId: String, triggerId: String) {
        apps.firstOrNull {
            appId == it.id
        }?.let { app ->
            LOGGER.warn("Command options not set. These options can be set using '/build-fleet BRANCH=<git-branch-name>(optional)  BUILD_TYPE=<release/debug>(optional)  FLAVOUR=<flavour>(optional)'")
            // Limit the list to 100. Slack limitation.
            val branchList = getFilteredReferences(appId).take(100)
            val buildTypesList = app.gradleTasks?.map {
                it.id
            }
            slackService.sendShowGenerateApkDialog(
                branchList,
                buildTypesList,
                null,
                triggerId,
                Constants.Slack.CALLBACK_GENERATE_APK + app.id,
                app
            )
        }
    }

    private suspend fun getReferences(appId: String): List<Reference>? {
        return cachingService.getCachedReferences(appId) ?: databaseService.getRefs(appId)?.map {
            LOGGER.debug("References: Cache miss")
            Reference(it.name, it.type)
        }.also { references ->
            if (references != null) {
                cachingService.cacheAppReferences(appId, references)
            }
        }
    }

    suspend fun showSubscriptionDialog(triggerId: String, channelId: String, vararg appsToFilter: App) {
        appsToFilter.toList().ifEmpty {
            this@ApplicationService.apps
        }.map {
            Pair(it, getFilteredReferences(it.id, RefType.BRANCH).take(20))
        }.filter {
            !it.second.isNullOrEmpty()
        }.takeIf {
            it.firstOrNull { !it.second.isNullOrEmpty() } != null
        }?.let { refs ->
            slackService.sendShowSubscriptionDialog(
                refs,
                triggerId
            )
        } ?: run {
            slackService.sendMessage("No Apps found for subscription", channelId, null)
        }
    }

    suspend fun showUnSubscriptionDialog(triggerId: String, slackUserId: String, channelId: String) {
        val refs = mutableMapOf<String, MutableSet<Reference>>()
        databaseService.findSubscriptionByUser(slackUserId)?.forEach { (app, ref) ->
            if(refs.containsKey(app.name)) {
                refs[app.name]?.add(Reference(ref.name, RefType.BRANCH))
            } else {
                refs[app.name] = mutableSetOf(Reference(ref.name, RefType.BRANCH))
            }
        }
        if(refs.isNotEmpty()) {
            slackService.sendShowUnSubscriptionDialog(refs.map { (key, value) ->
                Pair(apps.first {
                    it.id == key
                }, value)
            }, triggerId)
        } else {
            slackService.sendMessage("No subscriptions found", channelId, null)
        }
    }

    suspend fun handleSlackEvent(payload: String) = coroutineScope {
        val slackEvent = gson.fromJson(payload, SlackEvent::class.java)
        LOGGER.debug(slackEvent.toString())

        when (slackEvent.type) {
            Event.EventType.INTERACTIVE_MESSAGE -> {
                when (slackEvent.callbackId) {
                    // For standup bot
                    Constants.Slack.CALLBACK_STANDUP_MESSAGE -> {
                        slackService.showStandupPopup(slackEvent.triggerId!!)
                    }
                    else -> {
                        slackEvent.actions?.forEach { action ->
                            when {
                                // User confirmed APK Generation from dialog box
                                action.name?.startsWith(
                                    Constants.Slack.CALLBACK_CONFIRM_GENERATE_APK,
                                    true
                                ) == true -> {
                                    val appId = action.name?.substringAfter(Constants.Slack.CALLBACK_CONFIRM_GENERATE_APK, "")
                                    apps.firstOrNull {
                                        it.id == appId
                                    }?.let { app ->
                                        val callback = gson.fromJson(action.value, GenerateCallback::class.java)
                                        slackService.subscriptionResponse(app, callback, slackEvent,
                                            app.gradleTasks?.map {
                                                it.id
                                            })
                                    }
                                }

                                action.name?.startsWith(Constants.Slack.CALLBACK_SUBSCRIBE_BRANCH, true) == true -> {
                                    val updatedMessage = slackEvent.originalMessage?.copy(attachments = null)
                                    slackService.updateMessage(updatedMessage, slackEvent.channel?.id)
                                    try {
                                        val callback: CallbackMessage<String> = gson.fromJson<CallbackMessage<String>>(action.value, CallbackMessage::class.java)
                                        when(callback.action) {
                                            CallbackMessage.Action.POSITIVE -> {
                                                val appId = callback.data
                                                showSubscriptionDialog(
                                                    slackEvent.triggerId!!,
                                                    slackEvent.channel!!.id!!,
                                                    apps.first {
                                                        it.id == appId
                                                    })
                                            }
                                        }

                                    } catch (exception: Exception) {
                                        LOGGER.error("Something went wrong in callback", exception)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Event.EventType.MESSAGE_ACTION -> {
            }
            Event.EventType.DIALOG_SUBMISSION -> {
                onDialogSubmitted(slackEvent)
            }
            else -> {
                LOGGER.info("${slackEvent.type}")
            }
        }
    }

    private suspend fun onDialogSubmitted(slackEvent: SlackEvent) = withContext(Dispatchers.IO) {
        when {
            slackEvent.callbackId?.startsWith(Constants.Slack.CALLBACK_SUBSCRIBE_CONSUMER) == true -> {
                val callback = slackEvent.dialogResponse?.get(SlackConstants.TYPE_SELECT_BRANCH)
                    ?.split(Constants.Slack.NAME_SEPARATOR)
                if(callback?.size != 2) {
                    return@withContext
                }
                apps.firstOrNull {
                    it.id == callback.getOrNull(0)
                }?.let { app ->
                    val branch = callback.getOrNull(1)
                    val channelId = slackEvent.channel?.id
                    slackService.sendSubscribeToBranch(slackEvent, app, branch!!, channelId!!)
                }
            }
            slackEvent.callbackId?.startsWith(Constants.Slack.CALLBACK_UNSUBSCRIBE_CONSUMER) == true -> {
                val callback = slackEvent.dialogResponse?.get(SlackConstants.TYPE_SELECT_BRANCH)
                    ?.split(Constants.Slack.NAME_SEPARATOR)
                if(callback?.size != 2) {
                    return@withContext
                }
                apps.firstOrNull {
                    it.id == callback.getOrNull(0)
                }?.let { app ->
                    val branch = callback.getOrNull(1)
                    val channelId = slackEvent.channel?.id
                    slackService.unsubscribeFrom(slackEvent, app, branch!!, channelId!!)
                }
            }
            slackEvent.callbackId?.startsWith(Constants.Slack.CALLBACK_GENERATE_APK) == true -> {
                generateAppDialogResponse(slackEvent)
            }
            else -> {

            }
        }
    }

    private suspend fun generateAppDialogResponse(slackEvent: SlackEvent) = coroutineScope {
        slackEvent.callbackId?.substringAfter(Constants.Slack.CALLBACK_GENERATE_APK, "")?.let { appId ->
            apps.firstOrNull {
                it.id == appId
            }?.let { app ->
                if (!slackEvent.echoed.isNullOrEmpty()) {
                    launch(Dispatchers.IO) {
                        slackService.updateMessage(
                            slackEvent.echoed,
                            slackEvent.channel?.id!!
                        )
                    }
                }
                val buildData = slackEvent.dialogResponse?.filterValues { value ->
                    value != null
                }?.mapValues { map -> map.value as String }?.toMutableMap()

                launch(Dispatchers.IO) {
                    generateApk(buildData ?: mutableMapOf(), slackEvent.channel?.id ?: "general", app.id)
                }
            }
        }
    }

    /**
     * Update the cached reference list in redis for the given application
     *
     * @param app is the application whose reference needs to be updated.
     */
    private suspend fun updateCachedRefs(app: App) {
        databaseService.getRefs(app.id)?.map {
            Reference(it.name, it.type)
        }?.let {
            cachingService.cacheAppReferences(app.id, it)
        }

    }

    /**
     * Function to clear the app cache
     *
     * @param appId is the cached appId
     * @param branch is the branch whose APKs needs to be cleaned
     */
    private fun deleteApks(appId: String, branch: String) {
        GlobalScope.launch(Dispatchers.IO) {
            LOGGER.info("Deleting files for branch: $branch & appId: $appId")
            val appList = cachingService.getApkCache(appId, branch)
            // Clear stored list cache
            cachingService.deleteApkCache(appId, branch)
            appList.forEach {
                try {
                    if (it.pathOnDisk != null && File(it.pathOnDisk).deleteRecursively()) {
                        LOGGER.info("Deleted file ${it.pathOnDisk}")
                    }
                } catch (exception: Exception) {
                    LOGGER.error("Unable to delete apk at path ${it.pathOnDisk}", exception)
                }
            }
        }
    }

    suspend fun handleGithubEvent(appId: String, headers: Map<String, List<String>>, payload: Payload) = coroutineScope {
        apps.firstOrNull {
            it.id == appId
        }?.let { app ->
            when (headers[Constants.Github.HEADER_KEY_EVENT]?.first()) {
                Constants.Github.HEADER_VALUE_EVENT_PUSH -> payload.ref?.let { ref ->
                    val branch = ref.substringAfter("refs/heads/")
                    onCodePushed(app, Reference(branch, payload.refType ?: RefType.BRANCH))
                }
                Constants.Github.HEADER_VALUE_EVENT_CREATE -> {
                    when(val type = payload.refType) {
                        RefType.BRANCH, RefType.TAG -> {
                            payload.ref?.let {
                                onReferenceCreated(app, Reference(it, type))
                            }
                        }
                        else -> {}
                    }
                }
                Constants.Github.HEADER_VALUE_EVENT_DELETE -> {
                    when(val type = payload.refType) {
                        RefType.BRANCH, RefType.TAG -> {
                            payload.ref?.let {
                                onReferenceDeleted(app, Reference(it, type))
                            }
                        }
                        else -> {}
                    }
                }
                Constants.Github.HEADER_VALUE_EVENT_PING -> {
                }
                else -> {
                }
            }
        }
    }

    suspend fun handleBitbucketEvent(
        appId: String, headers: Map<String, List<String>>,
        body: BitbucketResponse
    ) {
        body.push?.changes?.firstOrNull()?.let {
            when {
                it.oldCommitData != null && it.newCommitData != null -> {
                    apps.firstOrNull { it.id == appId }?.let { app ->
                        it.newCommitData.getData(app)?.let { (app, ref) ->
                            onCodePushed(app, ref)
                        }
                    }
                }
                // A new branch is created
                it.oldCommitData == null -> {
                    apps.firstOrNull { it.id == appId }?.let { app ->
                        it.newCommitData.getData(app)?.let { (app, ref) ->
                            onReferenceCreated(app, ref)
                        }
                    }
                }
                // A branch is deleted
                it.newCommitData == null -> {
                    apps.firstOrNull { it.id == appId }?.let { app ->
                        it.oldCommitData.getData(app)?.let { (app, ref) ->
                            onReferenceDeleted(app, ref)
                        }
                    }
                }
                else -> {}
            }
        }
    }

    private suspend fun onCodePushed(app: App, reference: Reference) = withContext(Dispatchers.IO) {
        LOGGER.info("Removing apks for reference $reference for app ${app.name}")
        // Delete all apks present in cache
        deleteApks(app.id, reference.name)

        val subscriptions = databaseService.findSubscriptions(app.id, reference.name)
        subscriptions.orEmpty().forEach { resultRow ->
            slackService.sendShowConfirmGenerateApk(
                app,
                resultRow[Subscriptions.channel],
                resultRow[Refs.name],
                Constants.Slack.CALLBACK_CONFIRM_GENERATE_APK + app.id
            )
        }
    }

    private suspend fun onReferenceCreated(app: App, reference: Reference) = withContext(Dispatchers.IO) {
        LOGGER.info("Creating reference $reference for app ${app.name}")
        databaseService.addRef(app.id, reference)
        updateCachedRefs(app)
        fetchAndUpdateReferences(app)
    }

    private suspend fun onReferenceDeleted(app: App, reference: Reference) = withContext(Dispatchers.IO) {
        LOGGER.info("Deleting reference $reference for app ${app.name}")
        slackService.unsubscribeDeletedBranch(app, reference)
        databaseService.deleteRef(app.id, reference)
        updateCachedRefs(app)
        // Delete cached APKs
        deleteApks(app.id, reference.name)
        fetchAndUpdateReferences(app)
    }

    suspend fun subscribeSlackEvent(slackEvent: SlackEvent) {
        slackService.subscribeSlackEvent(slackEvent)
    }

    fun clear() {
        apps.forEach {
            dockerService.killContainer(it)
        }
        databaseService.clear()
        cachingService.close()
    }

    private fun verifyAndCacheApp(fileToCache: File, appId: String, params: Map<String, String>) {
        takeIf {
            params.containsKey(SlackConstants.TYPE_SELECT_BRANCH) && fileToCache.exists()
        }?.let {
            cachingService.cacheApk(
                appId, params[SlackConstants.TYPE_SELECT_BRANCH] ?: error("Branch is missing"),
                ApkCache(params, fileToCache.absolutePath)
            )
        } ?: fileToCache.deleteRecursively()
    }

    suspend fun uploadApk(
        byteArray: ByteArray, params: Map<String, String?>,
        channelId: String, fileName: String = "app.apk",
        onFinish: (() -> Unit)? = null
    ) {
        val initialComment = params.map {
            "${it.key} = ${it.value}"
        }.joinToString("\n")
        slackService.uploadFile(byteArray, channelId, initialComment, onFinish, fileName)
    }

    private suspend fun reportFailure(app: App, channelId: String?, details: String?) = coroutineScope {
        channelId?.let {
            slackService.sendMessage(details ?: "Something went wrong", channelId, null)
        }
        onTaskCompleted(app.id)
        launch(Dispatchers.IO) {
            cleanApp(app)
            onTaskCompleted(app.id)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger("com.tombspawn.ApplicationService")
    }
}