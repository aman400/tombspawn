package com.tombspawn

import com.google.common.base.Optional
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.tombspawn.base.common.*
import com.tombspawn.base.extensions.toMap
import com.tombspawn.data.*
import com.tombspawn.di.qualifiers.AppCacheMap
import com.tombspawn.di.qualifiers.UploadDirPath
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
import com.tombspawn.models.redis.ApkCallbackCache
import com.tombspawn.models.slack.Event
import com.tombspawn.models.slack.GenerateCallback
import com.tombspawn.models.slack.SlackEvent
import com.tombspawn.slackbot.SlackService
import com.tombspawn.utils.Constants
import kotlinx.coroutines.*
import org.apache.http.client.utils.URIBuilder
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import javax.inject.Inject

class ApplicationService @Inject constructor(
    private val slack: Slack,
    private val common: Common,
    private val gson: Gson,
    private val databaseService: DatabaseService,
    private val apps: List<App>,
    private val dockerService: DockerService,
    private val slackService: SlackService,
    @UploadDirPath
    val uploadDirPath: String,
    @AppCacheMap
    val cacheMap: StringMap,
    val config: Optional<ServerConf>
) {

    private val randomWaitingMessages = listOf(
        "Utha le re Baghwan..",
        "Jai Maharashtra!!",
        "Try Holding your Breath!!",
        "Hold your horses!!",
        "Checking Anti-Camp Radius",
        "Creating Randomly Generated Feature",
        "Doing Something You Don't Wanna Know About",
        "Doing The Impossible",
        "Don't Panic",
        "Ensuring Everything Works Perfektly",
        "Generating Plans for Faster-Than-Light Travel",
        "Hitting Your Keyboard Won't Make This Faster",
        "In The Grey, No One Can Hear You Scream",
        "Loading, Don't Wait If You Don't Want To",
        "Preparing to Spin You Around Rapidly"
    )

    suspend fun init() = coroutineScope {
        addApps()
        fetchBotInfo()
        apps.forEachIndexed { index, app ->
            dockerService.createContainer(app, common.basePort + index)
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

    suspend fun fetchAppsData() = coroutineScope {
        apps.forEach {
            withContext(Dispatchers.IO) {
                fetchReferences(it)
                fetchFlavours(it)
                fetchBuildVariants(it)
            }
        }
    }

    suspend fun fetchReferences(app: App) {
        dockerService.fetchReferences(app)?.let {
            databaseService.addRefs(it, app.id)
            cacheMap.setData("${app.id}_references", gson.toJson(it, object: TypeToken<List<Reference>>() {}.type))
        }
    }

    suspend fun fetchFlavours(app: App) {
        dockerService.fetchFlavours(app)?.let {
            databaseService.addFlavours(it, app.id)
            cacheMap.setData("${app.id}_flavours", gson.toJson(it, object: TypeToken<List<String>>() {}.type))
        }
    }

    private suspend fun fetchBuildVariants(app: App) {
        dockerService.fetchBuildVariants(app)?.let {
            databaseService.addBuildVariants(it, app.id)
            cacheMap.setData("${app.id}_build_variants", gson.toJson(it, object: TypeToken<List<String>>() {}.type))
        }
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
        val additionalParams = buildData[SlackConstants.TYPE_ADDITIONAL_PARAMS]?.trim()

        additionalParams?.let {
            it.toMap()?.forEach { key, value ->
                if (!buildData.containsKey(key)) {
                    buildData[key] = value
                }
            }
        }

        val userAppPrefix = buildData[SlackConstants.TYPE_SELECT_APP_PREFIX]
        buildData.remove(SlackConstants.TYPE_SELECT_APP_PREFIX)
        val apkPrefix = "${userAppPrefix?.let {
            "$it-"
        } ?: ""}${System.currentTimeMillis()}"

        apps.firstOrNull {
            it.id == appID
        }?.let { app ->
            val callbackId = System.nanoTime()
            val uriBuilder = config.get()?.let {
                URIBuilder().setScheme(it.scheme ?: "http")
                    .setHost("docker.for.mac.localhost")
//                    .setHost(it.host ?: Constants.Common.DEFAULT_HOST)
                    .setPort(it.port ?: Constants.Common.DEFAULT_PORT)
            } ?: URIBuilder().setScheme("http")
                .setHost("docker.for.mac.localhost")
//                .setHost(Constants.Common.DEFAULT_HOST)
                .setPort(Constants.Common.DEFAULT_PORT)
            uriBuilder.path = "apps/${app.id}/callback/$callbackId"
            cacheMap.setData(
                callbackId.toString(), gson.toJson(
                    ApkCallbackCache(callbackId.toString(), responseUrl, channelId),
                    ApkCallbackCache::class.java
                ).toString()
            )
            val callbackUri = uriBuilder.build().toASCIIString()
            when (val response = dockerService.generateApp(
                app.id,
                "$callbackUri/success",
                "$callbackUri/failure",
                apkPrefix,
                buildData
            )) {
                is CallSuccess -> {
                    slackService.sendMessage(randomWaitingMessages.shuffled().first(), channelId, null)
                }
                is CallFailure -> {
                    response.throwable?.printStackTrace()
                    slackService.sendMessage(response.errorBody ?: "Unable to generate app.", channelId, null)
                }
                is CallError -> {
                    response.throwable?.printStackTrace()
                    slackService.sendMessage("Unable to generate app.", channelId, null)
                }
            }.exhaustive
        }
    }

    suspend fun showGenerateApkDialog(appId: String, triggerId: String) {
        apps.firstOrNull {
            appId == it.id
        }?.let { app ->
            LOGGER.warn("Command options not set. These options can be set using '/build-fleet BRANCH=<git-branch-name>(optional)  BUILD_TYPE=<release/debug>(optional)  FLAVOUR=<flavour>(optional)'")
            val branchList = getReferences(app.id)
            val flavourList = getFlavours(app.id)
            val buildTypesList = getBuildVariants(app.id)
            slackService.sendShowGenerateApkDialog(
                branchList,
                buildTypesList,
                flavourList,
                null,
                triggerId,
                Constants.Slack.CALLBACK_GENERATE_APK + app.id,
                app.appUrl ?: ""
            )
        }
    }

    suspend fun getFlavours(appId: String): List<String>? {
        return cacheMap.getData("${appId}_flavours")?.let {
            LOGGER.debug("Flavours: Cache hit")
            gson.fromJson<List<String>>(it, object: TypeToken<List<String>>() {}.type)
        } ?: databaseService.getFlavours(appId)?.map {
            LOGGER.debug("Flavours: Cache miss")
            it.name
        }
    }

    suspend fun getReferences(appId: String): List<Reference>? {
        return cacheMap.getData("${appId}_references")?.let {
            LOGGER.debug("References: Cache hit")
            gson.fromJson<List<Reference>>(it, object: TypeToken<List<Reference>>() {}.type)
        } ?: databaseService.getRefs(appId)?.map {
            LOGGER.debug("References: Cache miss")
            Reference(it.name, it.type)
        }
    }

    suspend fun getBuildVariants(appId: String): List<String>? {
        return cacheMap.getData("${appId}_build_variants")?.let {
            LOGGER.debug("Build Variants: Cache hit")
            gson.fromJson<List<String>>(it, object: TypeToken<List<String>>() {}.type)
        } ?: databaseService.getBuildTypes(appId)?.map {
            LOGGER.debug("Build Variants: Cache miss")
            it.name
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
        println(slackEvent.toString())

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
                                            databaseService.getFlavours(app.id)?.map {
                                                it.name
                                            }, databaseService.getBuildTypes(app.id)?.map {
                                                it.name
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
                JsonParser().parse(response).asJsonObject
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
                    generateApk(buildData ?: mutableMapOf(), slackEvent.channel?.id ?: "general",
                        app.id, slackEvent.responseUrl ?: "")
                }
            }
        }
    }

    suspend fun updatedCachedRefs(app: App) {
        databaseService.getRefs(app.id)?.map {
            Reference(it.name, it.type)
        }?.let {
            cacheMap.setData("${app.id}_references", gson.toJson(it, object: TypeToken<List<Reference>>() {}.type))
        }

    }

    suspend fun handleGithubEvent(headers: Map<String, List<String>>, payload: Payload) = coroutineScope {
        apps.firstOrNull {
            it.repoId == payload.repository?.id
        }?.let { app ->
            when (headers[Constants.Github.HEADER_KEY_EVENT]?.first()) {
                Constants.Github.HEADER_VALUE_EVENT_PUSH -> payload.ref?.let { ref ->
                    val branch = ref.substringAfter("refs/heads/")
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
                                updatedCachedRefs(app)
                            }
                        } else if (payload.refType == RefType.TAG) {
                            payload.ref?.let { ref ->
                                databaseService.addRef(app.id, Reference(ref, RefType.TAG))
                                updatedCachedRefs(app)
                            }
                        }
                    }
                }
                Constants.Github.HEADER_VALUE_EVENT_DELETE -> {
                    launch {
                        if (payload.refType == RefType.BRANCH) {
                            payload.ref?.let { ref ->
                                databaseService.deleteRef(app.id, Reference(ref, RefType.BRANCH))
                                updatedCachedRefs(app)
                            }
                        } else if (payload.refType == RefType.TAG) {
                            payload.ref?.let { ref ->
                                databaseService.deleteRef(app.id, Reference(ref, RefType.TAG))
                                updatedCachedRefs(app)
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
        databaseService.clear()
        cacheMap.close()
    }

    suspend fun uploadApk(apkCallback: Apps.App.Callback, receivedFile: File) {
        val callback = gson.fromJson<ApkCallbackCache>(cacheMap.getData(apkCallback.callbackId), ApkCallbackCache::class.java)
        cacheMap.delete(apkCallback.callbackId)
        if (receivedFile.exists()) {
            slackService.uploadFile(receivedFile, callback.channelId!!) {
                // Delete the file and parent directories after upload
                receivedFile.parentFile.deleteRecursively()
            }
        } else {
            LOGGER.error("APK Generated but file not found in the folder")
            LOGGER.error("Something went wrong")
            if (callback.responseUrl != null) {
                slackService.sendMessage(
                    callback.responseUrl,
                    RequestData(
                        response = "Something went wrong. Unable to generate the APK"
                    )
                )
            } else {
                slackService.sendMessage(
                    "Something went wrong. Unable to generate the APK",
                    callback.channelId!!,
                    null
                )
            }
        }
    }

    suspend fun reportFailure(apkCallback: Apps.App.Callback, errorResponse: ErrorResponse) {
        val callback = gson.fromJson<ApkCallbackCache>(cacheMap.getData(apkCallback.callbackId), ApkCallbackCache::class.java)
        cacheMap.delete(apkCallback.callbackId)
        slackService.sendMessage(errorResponse.details ?: "Something went wrong", callback.channelId!!, null)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ApplicationService::class.java)
    }
}