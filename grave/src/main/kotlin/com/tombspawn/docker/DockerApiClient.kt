package com.tombspawn.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallbackTemplate
import com.github.dockerjava.api.model.*
import com.github.dockerjava.core.command.BuildImageResultCallback
import com.github.dockerjava.core.command.EventsResultCallback
import com.github.dockerjava.core.command.LogContainerResultCallback
import com.google.gson.JsonObject
import com.google.protobuf.ByteString
import com.tombspawn.base.*
import com.tombspawn.base.common.*
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.base.extensions.await
import com.tombspawn.base.network.Common
import com.tombspawn.base.network.withRetry
import com.tombspawn.di.qualifiers.DockerHttpClient
import com.tombspawn.models.config.App
import com.tombspawn.utils.Constants
import io.grpc.stub.StreamObserver
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@AppScope
class DockerApiClient @Inject constructor(
    private val dockerClient: DockerClient,
    @DockerHttpClient private val dockerHttpClients: MutableMap<String, HttpClient>
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger("com.tombspawn.docker.DockerApiClient")
    }

    suspend fun generateApp(app: App, vararg params: Pair<String, String>): ByteString? =
        suspendCancellableCoroutine { continuation ->
            Common.createGrpcChannel(app.id, Constants.Common.DEFAULT_PORT).let { channel ->
                var byteData = ByteString.EMPTY
                ApplicationGrpc.newStub(channel).generateApp(GenerateAppRequest
                    .newBuilder()
                    .putAllBuildParams(params.toMap())
                    .build(), object : StreamObserver<GenerateAppResponse> {
                    override fun onNext(value: GenerateAppResponse?) {
                        LOGGER.debug("On application bytes received")
                        value?.data?.let {
                            byteData = byteData.concat(it)
                            ByteString.copyFrom(it.toByteArray())
                        }
                    }

                    override fun onError(t: Throwable?) {
                        LOGGER.error("Unable to transfer application bytes", t)
                        continuation.resume(null)
                    }

                    override fun onCompleted() {
                        LOGGER.debug("On application data transfer complete")
                        continuation.resume(byteData)
                        channel.shutdown()
                    }
                })
            }
        }

    suspend fun fetchReferences(app: App): List<Ref> = coroutineScope {
        Common.createGrpcChannel(app.id, Constants.Common.DEFAULT_PORT).let { channel ->
            val refs: List<Ref> = try {
                ApplicationGrpc.newBlockingStub(channel)
                    .fetchReferences(
                        ReferencesRequest.newBuilder()
                            .setBranchLimit(-1)
                            .setTagLimit(app.tagCount)
                            .build()
                    ).refList
            } catch (exception: Exception) {
                LOGGER.error("Unable to fetch references", exception)
                listOf()
            }
            channel.shutdown()
            return@coroutineScope refs

        }
    }

    suspend fun cleanApp(app: App, callbackUri: String): JsonObject? = coroutineScope {
        dockerHttpClients[app.id]?.let { client ->
            withRetry(20, 10000, -1) {
                val call = client.request<HttpResponse> {
                    method = HttpMethod.Post
                    url {
                        encodedPath = "/app/clean"
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
                    is ServerFailure -> {
                        LOGGER.error(response.errorBody, response.throwable)
                        null
                    }
                    is CallError -> {
                        LOGGER.error("Unable to clean app", response.throwable)
                        null
                    }
                }
            }
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
                when (ContainerState.from(it.state)) {
                    ContainerState.STARTED -> {
                        dockerClient.stopContainerCmd(it.id).exec()
                    }
                    else -> {
                        LOGGER.debug("Unable to stop ${it.state} container")
                    }
                }
            }
    }

    fun restartContainer(tag: String) {
        dockerClient.listContainersCmd().withNameFilter(listOf(tag))
            .withShowAll(true)
            .exec().firstOrNull()?.let {
                when (ContainerState.from(it.state)) {
                    ContainerState.STARTED -> {
                        dockerClient.restartContainerCmd(it.id).exec()
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
                when (ContainerState.from(it.state)) {
                    ContainerState.PAUSED,
                    ContainerState.EXITED,
                    ContainerState.DEAD -> {
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
            @Suppress("BlockingMethodInNonBlockingContext")
            dockerClient.listImagesCmd().withImageNameFilter(tag).exec().firstOrNull() ?: dockerClient.buildImageCmd(
                    file
                )
                .withTags(setOf(tag))
                .withQuiet(false)
                .exec(object : BuildImageResultCallback() {
                    override fun onNext(item: BuildResponseItem?) {
                        super.onNext(item)
                        item?.let { buildResponse ->
                            buildResponse.stream?.let {
                                LOGGER.trace(it)
                            }
                            buildResponse.errorDetail?.let { error ->
                                LOGGER.error("${error.code} : ${error.message}")
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

    suspend fun logEvents(onEvent: ((event: Event) -> Unit)? = null) {
        coroutineScope {
            val callback = object : EventsResultCallback() {
                override fun onNext(event: Event) {
                    onEvent?.invoke(event) ?: LOGGER.trace("Event: $event")
                    super.onNext(event)
                }
            }
            dockerClient.eventsCmd().exec(callback)
        }
    }

    suspend fun createContainer(
        image: String, name: String, commands: List<String>?, volumes: List<Volume>?, volumeBinds: List<Bind>?,
        portBindings: Ports, exposedPorts: List<ExposedPort>?, memory: Long?, swapMemory: Long?, cpuShares: Int? = null,
        env: List<String>? = null
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
                        if (!env.isNullOrEmpty()) {
                            withEnv(env)
                        }
                        if (!exposedPorts.isNullOrEmpty()) {
                            withExposedPorts(exposedPorts)
                        }
                    }
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withAttachStdin(false)
                    .withHostConfig(
                        HostConfig.newHostConfig()
                            .apply {
                                if (memory != null) {
                                    withMemory(memory)
                                }
                                if (swapMemory != null) {
                                    withMemorySwap(swapMemory)
                                }
                            }
                            .withRestartPolicy(RestartPolicy.noRestart())
                            .apply {
                                if (cpuShares != null) {
                                    withCpuShares(cpuShares)
                                }
                                if (memory != null) {
                                    withMemoryReservation(memory / 2)
                                }
                                if (!volumeBinds.isNullOrEmpty()) {
                                    withBinds(volumeBinds)
                                }
                            }
                            .withPortBindings(portBindings)
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
                    when (ContainerState.from(it.state)) {
                        ContainerState.PAUSED,
                        ContainerState.EXITED,
                        ContainerState.DEAD,
                        ContainerState.CREATED -> {
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