package com.tombspawn

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.tombspawn.data.Api
import com.tombspawn.data.DatabaseService
import com.tombspawn.data.Refs
import com.tombspawn.data.Subscriptions
import com.tombspawn.docker.DockerService
import com.tombspawn.models.Reference
import com.tombspawn.models.RequestData
import com.tombspawn.models.config.App
import com.tombspawn.models.config.Common
import com.tombspawn.models.config.Slack
import com.tombspawn.models.github.Payload
import com.tombspawn.models.github.RefType
import com.tombspawn.models.slack.Event
import com.tombspawn.models.slack.SlackEvent
import com.tombspawn.models.slack.SlackMessage
import com.tombspawn.slackbot.*
import com.tombspawn.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject

class ApplicationService @Inject constructor(
    private val slack: Slack,
    private val common: Common,
    private val gson: Gson,
    private val databaseService: DatabaseService,
    private val slackClient: SlackClient,
    private val apps: List<App>,
    private val dockerService: DockerService
) {

    suspend fun init() = coroutineScope {
        addApps()
        fetchBotInfo()
        apps.forEachIndexed { index, app ->
            dockerService.createContainer(app, common.basePort + index)
            launch(Dispatchers.IO) {
                // Add app start delay
                delay(30000)
                fetchFlavours(app)
                fetchReferences(app)
            }
        }
        addVerbs()
        updateUserData()
    }

    private suspend fun fetchBotInfo() {
        slackClient.fetchBotData(slack.botToken)?.let { about ->
            databaseService.addUser(about.id!!, about.name, typeString = Constants.Database.USER_TYPE_BOT)
        }
    }

    private suspend fun addApps() {
        return databaseService.addApps(apps)
    }

    private suspend fun fetchReferences(app: App) {
        dockerService.fetchReferences(app)?.let {
            databaseService.addRefs(it, app.id)
        }
    }

    private suspend fun fetchFlavours(app: App) {
        dockerService.fetchFlavours(app)?.let {
            databaseService.addFlavours(it, app.id)
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
        val users = slackClient.getSlackUsers(slack.botToken, slackClient, null)
        val ims = slackClient.getSlackBotImIds(slack.botToken, slackClient, null)
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

    suspend fun generateAndUploadApk(
        buildData: MutableMap<String, String>?,
        channelId: String,
        appID: String,
        responseUrl: String
    ) {
        apps.firstOrNull {
            it.id == appID
        }?.let { app ->
            slackClient.generateAndUploadApk(buildData, channelId, app, responseUrl)
        }
    }

    suspend fun showGenerateApkDialog(appId: String, triggerId: String) {
        apps.firstOrNull {
            appId == it.id
        }?.let { app ->
            LOGGER.warn("Command options not set. These options can be set using '/build-fleet BRANCH=<git-branch-name>(optional)  BUILD_TYPE=<release/debug>(optional)  FLAVOUR=<flavour>(optional)'")
            val branchList = databaseService.getRefs(app.id)?.map {
                Reference(it.name, it.type)
            }
            val flavourList = databaseService.getFlavours(app.id)
            val buildTypesList = databaseService.getBuildTypes(app.id)
            slackClient.sendShowGenerateApkDialog(
                branchList,
                buildTypesList?.map { buildType -> buildType.name },
                flavourList?.map { flavour -> flavour.name },
                null,
                triggerId,
                Constants.Slack.CALLBACK_GENERATE_APK + app.id,
                app.appUrl ?: ""
            )
        }
    }

    suspend fun showSubscriptionDialog(appId: String, triggerId: String) {
        apps.firstOrNull {
            it.id == appId
        }?.let { app ->
            slackClient.sendShowSubscriptionDialog(
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
                        showStandupPopup(slackClient, slackEvent)
                    }
                    else -> {
                        slackEvent.actions?.forEach { action ->
                            when {
                                // User confirmed APK Generation from dialog box
                                action.name?.startsWith(
                                    Constants.Slack.CALLBACK_CONFIRM_GENERATE_APK,
                                    true
                                ) == true -> {
                                    subscriptionResponse(action, slackClient, slackEvent, databaseService, apps, gson)
                                }
                            }
                        }
                    }
                }
            }
            Event.EventType.MESSAGE_ACTION -> {
                when (slackEvent.callbackId) {
                    Constants.Slack.TYPE_CREATE_MOCK_API -> {
                        slackClient.sendShowCreateApiDialog(databaseService.getVerbs(), slackEvent.triggerId!!)
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
                sendSubscribeToBranch(slackEvent, slackClient, databaseService, apps)
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

                slackClient.sendMessage(
                    slackEvent.responseUrl!!,
                    RequestData(response = "Your `$verb` call is ready with url `${common.baseUrl}api/mock/$id`")
                )
            } catch (exception: Exception) {
                slackClient.sendMessage(
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
                        slackClient.updateMessage(
                            gson.fromJson(slackEvent.echoed, SlackMessage::class.java),
                            slackEvent.channel?.id!!
                        )
                    }
                }
                val buildData = slackEvent.dialogResponse?.filterValues { value ->
                    value != null
                }?.mapValues { map -> map.value as String }?.toMutableMap()

                launch(Dispatchers.IO) {
                    slackClient.generateAndUploadApk(
                        buildData,
                        slackEvent.channel?.id ?: "general",
                        app,
                        slackEvent.responseUrl
                    )
                }
            }
        }
    }

    suspend fun handleGithubEvent(headers: Map<String, List<String>>, payload: Payload) = coroutineScope {
        apps.firstOrNull {
            it.repoId == payload.repository?.id
        }?.let { app ->
            when(headers[Constants.Github.HEADER_KEY_EVENT]?.first()) {
                 Constants.Github.HEADER_VALUE_EVENT_PUSH -> payload.ref?.let { ref ->
                    val branch = ref.substringAfter("refs/heads/")
                    val subscriptions = databaseService.findSubscriptions(branch, app.id)
                    subscriptions.orEmpty().forEach { resultRow ->
                        launch(Dispatchers.IO) {
                            slackClient.sendShowConfirmGenerateApk(
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
                            }
                        } else if (payload.refType == RefType.TAG) {
                            payload.ref?.let { ref ->
                                databaseService.addRef(app.id, Reference(ref, RefType.TAG))
                            }
                        }
                    }
                }
                Constants.Github.HEADER_VALUE_EVENT_DELETE -> {
                    launch {
                        if (payload.refType == RefType.BRANCH) {
                            payload.ref?.let { ref ->
                                databaseService.deleteRef(app.id, Reference(ref, RefType.BRANCH))
                            }
                        } else if (payload.refType == RefType.TAG) {
                            payload.ref?.let { ref ->
                                databaseService.deleteRef(app.id, Reference(ref, RefType.TAG))
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
        slackClient.subscribeSlackEvent(databaseService, slackEvent)
    }

    suspend fun getApi(apiId: String, verb: String): Api? {
        return databaseService.getApi(apiId, verb)
    }

    suspend fun sendShowCreateApiDialog(triggerId: String) {
        slackClient.sendShowCreateApiDialog(databaseService.getVerbs(), triggerId)
    }

    fun clear() {
        databaseService.clear()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ApplicationService::class.java)
    }
}