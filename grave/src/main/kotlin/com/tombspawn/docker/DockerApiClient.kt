package com.tombspawn.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallbackTemplate
import com.github.dockerjava.api.model.*
import com.github.dockerjava.core.command.BuildImageResultCallback
import com.github.dockerjava.core.command.EventsResultCallback
import com.github.dockerjava.core.command.LogContainerResultCallback
import com.google.gson.JsonObject
import com.tombspawn.base.common.*
import com.tombspawn.base.extensions.await
import com.tombspawn.base.network.withRetry
import com.tombspawn.di.qualifiers.DockerHttpClient
import com.tombspawn.models.Reference
import com.tombspawn.models.config.App
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.http.HttpMethod
import io.ktor.http.parametersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

class DockerApiClient @Inject constructor(
    private val dockerClient: DockerClient,
    @DockerHttpClient private val dockerHttpClients: MutableMap<String, HttpClient>
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger("com.tombspawn.docker.DockerApiClient")
        const val STATE_STARTED = "running"
        const val STATE_CREATED = "created"
        const val STATE_RESTARTING = "restarting"
        const val STATE_PAUSED = "paused"
        const val STATE_EXITED = "exited"
        const val STATE_DEAD = "dead"
    }

    suspend fun fetchFlavours(app: App, callbackUri: String): JsonObject? = coroutineScope {
        dockerHttpClients[app.id]?.let { client ->
            withRetry(3, 10000, -1) {
                val call = client.call {
                    method = HttpMethod.Get
                    url {
                        encodedPath = "/flavours"
                        parameters.append(CommonConstants.CALLBACK_URI, callbackUri)
                    }
                }
                call.await<JsonObject>()
            }.let { response ->
                when (response) {
                    is CallSuccess -> {
                        response.data
                    }
                    is CallFailure -> {
                        LOGGER.error(response.errorBody, response.throwable)
                        null
                    }
                    is CallError -> {
                        LOGGER.error("Unable to fetch flavours", response.throwable)
                        null
                    }
                }
            }
        }
    }

    suspend fun fetchBuildVariants(app: App, callbackUri: String): JsonObject? = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO) {
            dockerHttpClients[app.id]?.let { client ->
                val call = client.call {
                    method = HttpMethod.Get
                    url {
                        encodedPath = "/build-variants"
                        parameters.append(CommonConstants.CALLBACK_URI, callbackUri)
                    }
                }
                return@withContext when (val response = call.await<JsonObject>()) {
                    is CallSuccess -> {
                        response.data?.let {
                            it
                        }
                    }
                    is CallFailure -> {
                        LOGGER.error(response.errorBody)
                        null
                    }
                    is CallError -> {
                        LOGGER.error("Unable to fetch build variants", response.throwable)
                        null
                    }
                }
            }
        }
    }

    suspend fun generateApp(appId: String, vararg params: Pair<String, List<String>>): Response<JsonObject> = coroutineScope {
        dockerHttpClients[appId]?.let { client ->
            return@coroutineScope client.call {
                method = HttpMethod.Get
                url {
                    encodedPath = "/app/generate"
                    parameters.appendAll(parametersOf(*params))
                }
            }.await<JsonObject>()
        } ?: CallFailure("Http Client not found")
    }

    suspend fun fetchReferences(app: App, callbackUri: String): JsonObject? = coroutineScope {
        dockerHttpClients[app.id]?.let { client ->
            withRetry(20, 10000, -1) {
                val call = client.call {
                    method = HttpMethod.Get
                    url {
                        encodedPath = "/references"
                        parameters.append(CommonConstants.CALLBACK_URI, callbackUri)
                    }
                }
                call.await<JsonObject>()
            }.let { response ->
                when (response) {
                    is CallSuccess -> {
                        response.data
                    }
                    is CallFailure -> {
                        LOGGER.error(response.errorBody)
                        null
                    }
                    is CallError -> {
                        LOGGER.error("Unable to fetch References", response.throwable)
                        null
                    }
                }
            }
        }
    }

    suspend fun listContainer(): MutableList<Container>? {
        return suspendCoroutine {
            dockerClient.listContainersCmd()
                .withShowSize(true)
                .withShowAll(true).exec()
        }
    }

    suspend fun createVolume(name: String, driver: String = "local"): String {
        return coroutineScope<String> {
            dockerClient.listVolumesCmd()
                .exec().volumes.firstOrNull {
                it.name == name
            }?.name ?: dockerClient.createVolumeCmd()
                .withName(name)
                .withDriver(driver)
                .exec().name
        }
    }

    fun stopContainer(tag: String) {
        dockerClient.listContainersCmd().withNameFilter(listOf(tag))
            .withShowAll(true)
            .exec().firstOrNull()?.let {
                when (it.state) {
                    STATE_STARTED -> {
                        dockerClient.stopContainerCmd(it.id).exec()
                    }
                    else -> {
                        LOGGER.debug("Unable to stop ${it.state} container")
                    }
                }
            }
    }

    fun killContainer(tag: String) {
        dockerClient.listContainersCmd().withNameFilter(listOf(tag))
            .withShowAll(true)
            .exec().firstOrNull()?.let {
                when (it.state) {
                    STATE_PAUSED,
                    STATE_EXITED,
                        STATE_DEAD -> {
                        dockerClient.killContainerCmd(it.id).exec()
                    }
                    else -> {
                        LOGGER.debug("Unable to stop ${it.state} container")
                    }
                }
            }
    }

    suspend fun createImage(file: File, tag: String) {
        coroutineScope {
            dockerClient.listImagesCmd().withImageNameFilter(tag).exec().firstOrNull() ?: dockerClient.buildImageCmd(
                file
            )
                .withTags(setOf(tag))
                .withQuiet(false)
                .exec(object : BuildImageResultCallback() {
                    override fun onNext(item: BuildResponseItem?) {
                        super.onNext(item)
                        item?.let {
                            it.stream?.let {
                                LOGGER.trace(it)
                            }
                            it.errorDetail?.let {
                                LOGGER.error("${it.code} : ${it.message}")
                            }
                        }
                    }
                }).awaitCompletion()
        }
    }

    suspend fun createNetwork(name: String, driver: String = "bridge"): String {
        return coroutineScope<String> {
            return@coroutineScope dockerClient.listNetworksCmd().withNameFilter(name).exec().firstOrNull()?.id
                ?: dockerClient.createNetworkCmd().withDriver(driver)
                    .withCheckDuplicate(true).withName(name).exec().id
        }
    }

    suspend fun attachNetwork(containerId: String, networkId: String) {
        coroutineScope {
            dockerClient.listContainersCmd()
                .withIdFilter(listOf(containerId))
                .exec().firstOrNull {
                    it.networkSettings?.networks?.entries?.firstOrNull {
                        it.value.networkID == networkId
                    } != null
                } ?: dockerClient.connectToNetworkCmd()
                .withContainerId(containerId)
                .withNetworkId(networkId).exec()
        }
    }

    suspend fun logContainerCommand(containerName: String, containerId: String) {
        coroutineScope {
            dockerClient
                .logContainerCmd(containerName)
                .withContainerId(containerId)
                .withFollowStream(true)
                .withStdErr(true)
                .withStdOut(true)
                .exec(object : ResultCallbackTemplate<LogContainerResultCallback, Frame>() {
                    override fun onNext(frame: Frame?) {
                        frame?.payload?.let {
                            LOGGER.trace(String(it))
                        }
                    }
                })
        }
    }

    suspend fun logEvents() {
        coroutineScope {
            val callback = object : EventsResultCallback() {
                override fun onNext(event: Event) {
                    LOGGER.trace("Event: $event")
                    super.onNext(event)
                }
            }
            dockerClient.eventsCmd().exec(callback)
        }
    }

    suspend fun createContainer(
        image: String, name: String, commands: List<String>?, volumes: List<Volume>?, volumeBinds: List<Bind>?,
        portBindings: Ports, exposedPorts: List<ExposedPort>?
    ): String? {
        return coroutineScope<String?> {
            return@coroutineScope dockerClient.listContainersCmd().withShowAll(true)
                .withNameFilter(listOf(name))
                .exec().firstOrNull()?.id ?: run {
                dockerClient.createContainerCmd(name)
                    .withImage(image)
                    .withName(name)
                    .apply {
                        if (!volumes.isNullOrEmpty()) {
                            withVolumes(volumes)
                        }
                        if (!commands.isNullOrEmpty()) {
                            withCmd(commands)
                        }
                    }
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withAttachStdin(false)
                    .withExposedPorts(exposedPorts)
                    .withHostConfig(
                        HostConfig.newHostConfig()
                            .withPortBindings(portBindings)
                            .withBinds(volumeBinds)
                    )
                    .exec().let {
                        it.warnings.forEach { message ->
                            LOGGER.warn(message)
                        }
                        it.id
                    }
            }
        }
    }

    suspend fun startContainer(id: String) {
        coroutineScope {
            dockerClient.listContainersCmd().withIdFilter(listOf(id))
                .withShowAll(true)
                .exec().firstOrNull()?.let {
                    when (it.state) {
                        STATE_PAUSED,
                        STATE_EXITED,
                        STATE_DEAD,
                        STATE_CREATED -> {
                            dockerClient.startContainerCmd(id).exec()
                        }
                        else -> {
                            LOGGER.info("Not starting ${it.state} Container")
                        }
                    }
                }
        }
    }
}