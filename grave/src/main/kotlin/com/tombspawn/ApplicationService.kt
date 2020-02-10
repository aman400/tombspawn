package com.tombspawn

import com.google.common.base.Optional
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.tombspawn.base.common.ErrorResponse
import com.tombspawn.base.common.SlackConstants
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.base.extensions.toMap
import com.tombspawn.data.*
import com.tombspawn.data.cache.models.ApkCache
import com.tombspawn.di.qualifiers.ApplicationBaseUri
import com.tombspawn.di.qualifiers.Debuggable
import com.tombspawn.di.qualifiers.UploadDir
import com.tombspawn.docker.DockerService
import com.tombspawn.models.Reference
import com.tombspawn.models.RequestData
import com.tombspawn.models.config.App
import com.tombspawn.models.config.Common
import com.tombspawn.models.config.ServerConf
import com.tombspawn.models.config.Slack
import com.tombspawn.models.github.Payload
import com.tombspawn.models.github.RefType
import com.tombspawn.models.locations.Apps
import com.tombspawn.models.slack.Event
import com.tombspawn.models.slack.GenerateCallback
import com.tombspawn.models.slack.SlackEvent
import com.tombspawn.slackbot.SlackService
import com.tombspawn.utils.Constants
import io.ktor.http.URLBuilder
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Provider

@AppScope
class ApplicationService @Inject constructor(
    private val slack: Slack,
    private val common: Common,
    private val gson: Gson,
    private val databaseService: DatabaseService,
    private val apps: List<App>,
    private val dockerService: DockerService,
    private val slackService: SlackService,
    @UploadDir
    val uploadDir: File,
    private val cachingService: CachingService,
    @Debuggable
    val debug: Boolean,
    val config: Optional<ServerConf>,
    @ApplicationBaseUri
    val baseUri: Provider<URLBuilder>
) {

    private val randomWaitingMessages = listOf(
        "Try Holding your Breath!!",
        "Hold your horses!!",
        "Creating Randomly Generated Feature",
        "Doing Something You Don't Wanna Know About",
        "Doing The Impossible",
        "Don't Panic",
        "Ensuring Everything Works Perfektly",
        "Generating Plans for Faster-Than-Light Travel",
        "Hitting Your Keyboard Won't Make This Faster",
        "Loading, Don't Wait If You Don't Want To",
        "You usually have to wait for that which is worth waiting for",
        "The waiting time, my brothers, Is the hardest time of all",
        "Waiting is a trap. There will always be reasons to wait",
        "Don't Wait! Start on your dreams",
        "How much of human life is lost in waiting.",
        "If you wait for App, App comes. If you don't wait for App, App still comes.",
        "All things come to him who waits - provided he knows what he is waiting for."
    )

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
        launch {
            cleanAppCacheFiles()
        }
        apps.forEachIndexed { index, app ->
            val callbackUri = baseUri.get().path("apps", app.id, "init").build().toString()
            dockerService.createContainer(app, common.basePort + index, callbackUri)
        }

        addVerbs()
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

    suspend fun fetchAppData(appId: String) {
        apps.firstOrNull {
            appId == it.id
        }?.let {
            withContext(Dispatchers.IO) {
                runBlocking {
                    dockerService.appInitialized(it)
                }
                fetchReferences(it)
            }
        }
    }

    /**
     * Clean the files cache directory
     */
    private suspend fun cleanAppCacheFiles() = coroutineScope {
        launch(Dispatchers.IO) {
            try {
                uploadDir.let {
                    if(it.exists()) {
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
    private suspend fun fetchReferences(app: App) {
        val callbackUri = baseUri.get().path("apps", app.id, "refs").build().toString()
        dockerService.fetchReferences(app, callbackUri)
    }

    /**
     * Run clean command from given application
     *
     * @param app is app container to clean.
     */
    private suspend fun cleanApp(app: App) {
        val callbackUri = baseUri.get().path("apps", app.id, "clean").build().toString()
        dockerService.cleanApp(app, callbackUri)
    }

    /**
     * Function to fetch app flavours.
     *
     * @param app is the application for which the flavours needs to be fetched
     */
    private suspend fun fetchFlavours(app: App) {
        val callbackUri = baseUri.get().path("apps", app.id, "flavours").build().toString()
        dockerService.fetchFlavours(app, callbackUri)
    }

    /**
     * Function to add reference(branches/tags) to redis and database
     *
     * @param appId is app to be cached
     * @param refs is the list of references to be cached to redis and database
     */
    suspend fun addRefs(appId: String, refs: List<Reference>) {
        databaseService.addRefs(refs, appId)
        cachingService.cacheAppReferences(appId, refs)
    }

    suspend fun addBuildVariants(appId: String, buildVariants: List<String>) {
        databaseService.addBuildVariants(buildVariants, appId)
        cachingService.cacheBuildVariants(appId, buildVariants)
    }

    suspend fun addFlavours(appId: String, flavours: List<String>) {
        databaseService.addFlavours(flavours, appId)
        cachingService.cacheAppFlavours(appId, flavours)
    }

    private suspend fun fetchBuildVariants(app: App) {
        val callbackUri = baseUri.get().path(listOf("apps", app.id, "build-variants")).build().toString()
        dockerService.fetchBuildVariants(app, callbackUri)
    }

    private suspend fun addVerbs() {
        databaseService.addVerbs(
            listOf(
                Constants.Common.GET,
                Constants.Common.PUT,
                Constants.Common.POST,
                Constants.Common.DELETE,
                Constants.Common.PATCH,
                Constants.Common.HEAD,
                Constants.Common.OPTIONS
            )
        )
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
        appID: String,
        responseUrl: String
    ) {
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
            // Unique callback id
            val callbackId = System.nanoTime().toString()
            // Base url for callback
            val callbackUri = baseUri.get().path("apps", app.id, "callback", callbackId).build().toString()

            val useCache: Boolean = app.gradleTasks?.firstOrNull {
                it.id == buildData[SlackConstants.TYPE_SELECT_BUILD_TYPE]
            }?.useCache ?: true

            // Save the application generation cache.
            cachingService.saveAppCallbackCache(callbackId, responseUrl, channelId, useCache)
            LOGGER.debug("CallbackUri: %s", callbackUri)

            LOGGER.debug(if(useCache) "Using cache" else "Skipping cache")

            val toVerify = ApkCache(buildData)
            if(!useCache || !verifyAndUploadCachedApk(buildData, toVerify, app, callbackId)) {
                // Generate the application
                dockerService.generateApp(
                    app.id,
                    "$callbackUri/success",
                    "$callbackUri/failure",
                    buildData,
                    // verify if app was generated from some queued request
                    verify = {
                        if(useCache) {
                            verifyAndUploadCachedApk(buildData, toVerify, app, callbackId)
                        } else {
                            false
                        }
                    }
                )
                slackService.sendMessage(randomWaitingMessages.shuffled().first(), channelId, null)
            }
        }
    }

    private suspend fun verifyAndUploadCachedApk(buildData: MutableMap<String, String>,
                                                 apkCache: ApkCache, app: App, callbackId: String) = coroutineScope {
        val cache = buildData[SlackConstants.TYPE_SELECT_BRANCH]?.let {
            cachingService.getApkCache(app.id, it)
        }?.firstOrNull {
            it == apkCache
        }?.let {
            Pair(it, it.pathOnDisk?.let { File(it) })
        }
        if (cache?.second?.exists() == true) {
            launch(Dispatchers.IO) {
                uploadApk(
                    Apps.App.Callback(Apps.App(app.id), callbackId),
                    cache.second!!,
                    cache.first.params.toMutableMap(),
                    // Do not cache the apk again
                    false
                )
            }
            return@coroutineScope true
        } else {
            return@coroutineScope false
        }
    }

    suspend fun showGenerateApkDialog(appId: String, triggerId: String) {
        apps.firstOrNull {
            appId == it.id
        }?.let { app ->
            LOGGER.warn("Command options not set. These options can be set using '/build-fleet BRANCH=<git-branch-name>(optional)  BUILD_TYPE=<release/debug>(optional)  FLAVOUR=<flavour>(optional)'")
            val branchList = getReferences(app.id)
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

    private suspend fun getFlavours(appId: String): List<String>? {
        return cachingService.getCachedFlavours(appId) ?: databaseService.getFlavours(appId)?.map {
            LOGGER.debug("Flavours: Cache miss")
            it.name
        }.also { flavours ->
            if (flavours != null) {
                cachingService.cacheAppFlavours(appId, flavours)
            }
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

    private suspend fun getBuildVariants(appId: String): List<String>? {
        return cachingService.getBuildVariants(appId) ?: databaseService.getBuildTypes(appId)?.map {
            LOGGER.debug("Build Variants: Cache miss")
            it.name
        }.also { buildVariants ->
            if (buildVariants != null) {
                cachingService.cacheBuildVariants(appId, buildVariants)
            }
        }
    }

    suspend fun showSubscriptionDialog(appId: String, triggerId: String) {
        apps.firstOrNull {
            it.id == appId
        }?.let { app ->
            slackService.sendShowSubscriptionDialog(
                databaseService.getRefs(app.id),
                triggerId,
                app
            )
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
                                    val appId =
                                        action.name?.substringAfter(Constants.Slack.CALLBACK_CONFIRM_GENERATE_APK, "")
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
                            }
                        }
                    }
                }
            }
            Event.EventType.MESSAGE_ACTION -> {
                when (slackEvent.callbackId) {
                    SlackConstants.TYPE_CREATE_MOCK_API -> {
                        slackService.sendShowCreateApiDialog(slackEvent.triggerId!!)
                    }
                    else -> {

                    }
                }
            }
            Event.EventType.DIALOG_SUBMISSION -> {
                onDialogSubmitted(slackEvent)
            }
            else -> {
                LOGGER.info("${slackEvent.type}")
            }
        }
    }

    private suspend fun onDialogSubmitted(slackEvent: SlackEvent) = coroutineScope {
        when {
            slackEvent.callbackId?.startsWith(Constants.Slack.CALLBACK_SUBSCRIBE_CONSUMER) == true -> {
                val appId = slackEvent.callbackId.substringAfter(Constants.Slack.CALLBACK_SUBSCRIBE_CONSUMER)
                apps.firstOrNull {
                    it.id == appId
                }?.let { app ->
                    val branch = slackEvent.dialogResponse?.get(SlackConstants.TYPE_SELECT_BRANCH)
                    val channelId = slackEvent.channel?.id
                    slackService.sendSubscribeToBranch(slackEvent, app, branch!!, channelId!!)
                }
            }
            slackEvent.callbackId == Constants.Slack.CALLBACK_CREATE_API -> {
                createApiDialogResponse(slackEvent)
            }
            slackEvent.callbackId?.startsWith(Constants.Slack.CALLBACK_GENERATE_APK) == true -> {
                generateAppDialogResponse(slackEvent)
            }
            else -> {

            }
        }
    }

    private suspend fun createApiDialogResponse(slackEvent: SlackEvent) = coroutineScope {
        val verb = slackEvent.dialogResponse?.get(Constants.Slack.TYPE_SELECT_VERB)
        val response = slackEvent.dialogResponse?.get(Constants.Slack.TYPE_SELECT_RESPONSE)

        val id = UUID.randomUUID().toString().replace("-", "", true)

        launch(Dispatchers.IO) {
            try {
                JsonParser.parseString(response).asJsonObject
                databaseService.addApi(id, verb!!, response!!)

                slackService.sendMessage(
                    slackEvent.responseUrl!!,
                    RequestData(response = "Your `$verb` call is ready with url `${common.baseUrl}api/mock/$id`")
                )
            } catch (exception: Exception) {
                slackService.sendMessage(
                    slackEvent.responseUrl!!,
                    RequestData(response = "Invalid JSON")
                )
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
                    generateApk(
                        buildData ?: mutableMapOf(), slackEvent.channel?.id ?: "general",
                        app.id, slackEvent.responseUrl ?: ""
                    )
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
                    if(it.pathOnDisk != null && File(it.pathOnDisk).parentFile.deleteRecursively()) {
                        LOGGER.info("Deleted file ${it.pathOnDisk}")
                    }
                } catch (exception: Exception) {
                    LOGGER.error("Unable to delete apk at path ${it.pathOnDisk}", exception)
                }
            }
        }
    }

    suspend fun handleGithubEvent(headers: Map<String, List<String>>, payload: Payload) = coroutineScope {
        apps.firstOrNull {
            it.repoId == payload.repository?.id
        }?.let { app ->
            when (headers[Constants.Github.HEADER_KEY_EVENT]?.first()) {
                Constants.Github.HEADER_VALUE_EVENT_PUSH -> payload.ref?.let { ref ->
                    val branch = ref.substringAfter("refs/heads/")
                    // Delete all apks present in cache
                    deleteApks(app.id, branch)

                    val subscriptions = databaseService.findSubscriptions(branch, app.id)
                    subscriptions.orEmpty().forEach { resultRow ->
                        launch(Dispatchers.IO) {
                            slackService.sendShowConfirmGenerateApk(
                                resultRow[Subscriptions.channel],
                                resultRow[Refs.name],
                                Constants.Slack.CALLBACK_CONFIRM_GENERATE_APK + app.id
                            )
                        }
                    }
                }
                Constants.Github.HEADER_VALUE_EVENT_CREATE -> {
                    launch {
                        if (payload.refType == RefType.BRANCH) {
                            payload.ref?.let { ref ->
                                databaseService.addRef(app.id, Reference(ref, RefType.BRANCH))
                                updateCachedRefs(app)
                            }
                        } else if (payload.refType == RefType.TAG) {
                            payload.ref?.let { ref ->
                                databaseService.addRef(app.id, Reference(ref, RefType.TAG))
                                updateCachedRefs(app)
                            }
                        }
                    }
                }
                Constants.Github.HEADER_VALUE_EVENT_DELETE -> {
                    launch {
                        if (payload.refType == RefType.BRANCH) {
                            payload.ref?.let { ref ->
                                databaseService.deleteRef(app.id, Reference(ref, RefType.BRANCH))
                                updateCachedRefs(app)
                                // Delete cached APKs
                                deleteApks(app.id, ref)
                            }
                        } else if (payload.refType == RefType.TAG) {
                            payload.ref?.let { ref ->
                                databaseService.deleteRef(app.id, Reference(ref, RefType.TAG))
                                updateCachedRefs(app)
                                // Delete cached APKs
                                deleteApks(app.id, ref)
                            }
                        }
                    }
                    LOGGER.info("deleted branch: ${payload.ref}")
                }
                Constants.Github.HEADER_VALUE_EVENT_PING -> {
                }
                else -> {
                }
            }
        }
    }

    suspend fun subscribeSlackEvent(slackEvent: SlackEvent) {
        slackService.subscribeSlackEvent(slackEvent)
    }

    suspend fun getApi(apiId: String, verb: String): Api? {
        return databaseService.getApi(apiId, verb)
    }

    suspend fun sendShowCreateApiDialog(triggerId: String) {
        slackService.sendShowCreateApiDialog(triggerId)
    }

    fun clear() {
        apps.forEach {
            dockerService.killContainer(it)
        }
        databaseService.clear()
        cachingService.close()
    }

    private fun verifyAndCacheApp(appId: String, params: Map<String, String>, fileToCache: File) {
        params[SlackConstants.TYPE_SELECT_BRANCH]?.let { branch ->
            cachingService.cacheApk(appId, branch, ApkCache(params, fileToCache.absolutePath))
        } ?: fileToCache.parentFile.deleteRecursively()
    }

    suspend fun uploadApk(apkCallback: Apps.App.Callback, receivedFile: File,
                          params: MutableMap<String, String>, cacheApk: Boolean) {
        val callback = cachingService.getAppCallbackCache(apkCallback.callbackId)
        cachingService.clearAppCallback(apkCallback.callbackId)
        if (receivedFile.exists()) {
            LOGGER.debug("Params: %s", params)
            val data = params.map {
                "${it.key} = ${it.value}"
            }.joinToString("\n")
            callback?.channelId?.let { channelId ->
                LOGGER.info("Apk Cache hit")
                slackService.uploadFile(receivedFile, channelId, data) {
                    if(callback.useCache) {
                        if (cacheApk) {
                            verifyAndCacheApp(apkCallback.app.id, params, receivedFile)
                        }
                    } else {
                        receivedFile.parent.deleteRecursively()
                    }
                }
            } ?: run {
                if(callback?.useCache == true) {
                    if (cacheApk) {
                        verifyAndCacheApp(apkCallback.app.id, params, receivedFile)
                    }
                } else {
                    receivedFile.parent.deleteRecursively()
                }
                LOGGER.error("Channel id is null, Unable to upload file")
            }
        } else {
            LOGGER.error("APK Generated but file not found in the folder")
            if (callback?.responseUrl != null) {
                slackService.sendMessage(
                    callback.responseUrl,
                    RequestData(
                        response = "Something went wrong. Unable to generate the APK"
                    )
                )
            } else {
                callback?.channelId?.let { channelId ->
                    slackService.sendMessage("Something went wrong. Unable to generate the APK", channelId, null)
                }
            }
        }
    }

    suspend fun reportFailure(apkCallback: Apps.App.Callback, errorResponse: ErrorResponse) {
        val callback = cachingService.getAppCallbackCache(apkCallback.callbackId)
        cachingService.clearAppCallback(apkCallback.callbackId)
        callback?.channelId?.let { channelId ->
            slackService.sendMessage(errorResponse.details ?: "Something went wrong", channelId, null)
        }
        GlobalScope.launch {
            apps.firstOrNull {
                apkCallback.app.id == it.id
            }?.let {
                cleanApp(it)
            }
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ApplicationService::class.java)
    }
}