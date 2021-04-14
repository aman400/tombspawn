package com.tombspawn.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.command.BuildImageResultCallback
import com.github.dockerjava.api.model.*
import com.google.protobuf.ByteString
import com.tombspawn.base.*
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.base.network.Common
import com.tombspawn.models.config.App
import com.tombspawn.utils.Constants
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@AppScope
class DockerApiClient @Inject constructor(
    private val dockerClient: DockerClient
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger("com.tombspawn.docker.DockerApiClient")
    }

    suspend fun generateApp(app: App, vararg params: Pair<String, String>): GenerateAppResponse =
        suspendCancellableCoroutine { continuation ->
            Common.createGrpcChannel(app.id, Constants.Common.DEFAULT_PORT)
                .also { channel ->
                    val appResponseBuilder = GenerateAppResponse.newBuilder()
                    var byteData = ByteString.EMPTY
                    ApplicationGrpc.newStub(channel)
                        .withDeadlineAfter(45, TimeUnit.MINUTES)
                        .generateApp(GenerateAppRequest
                            .newBuilder()
                            .putAllBuildParams(params.toMap())
                            .build(), object : StreamObserver<GenerateAppResponse> {
                            override fun onNext(value: GenerateAppResponse?) {
                                LOGGER.debug("On application bytes received")
                                value?.data?.let {
                                    byteData = byteData.concat(it)
                                    ByteString.copyFrom(it.toByteArray())
                                }
                                value?.fileName?.let {
                                    appResponseBuilder.setFileName(it)
                                }
                                value?.responseParamsMap?.let {
                                    appResponseBuilder.putAllResponseParams(it)
                                }
                            }

                            override fun onError(t: Throwable?) {
                                byteData = ByteString.EMPTY
                                LOGGER.error("Unable to generate application", t)
                                continuation.resumeWithException(t ?: Exception("Unable to generate application"))
                            }

                            override fun onCompleted() {
                                LOGGER.debug("On application data transfer complete")
                                appResponseBuilder.data = byteData
                                continuation.resume(appResponseBuilder.build())
                                channel.shutdown()
                            }
                        })
                }
        }

    suspend fun fetchReferences(app: App): List<Ref> = suspendCancellableCoroutine { continuation ->
        Common.createGrpcChannel(app.id, Constants.Common.DEFAULT_PORT).let { channel ->
            ApplicationGrpc.newStub(channel)
                .withDeadlineAfter(20, TimeUnit.MINUTES)
                .fetchReferences(
                    ReferencesRequest.newBuilder()
                        .setBranchLimit(app.branchCount)
                        .setTagLimit(app.tagCount)
                        .build(), object : StreamObserver<ReferencesResponse> {
                        override fun onNext(value: ReferencesResponse?) {
                            value?.refList?.let {
                                continuation.resume(it)
                            } ?: continuation.resume(listOf())
                        }

                        override fun onError(t: Throwable?) {
                            LOGGER.error("Unable to fetch references", t)
                            continuation.resume(listOf())
                        }

                        override fun onCompleted() {
                            channel.shutdown()
                        }
                    })
        }
    }

    suspend fun cleanApp(app: App): Boolean = suspendCancellableCoroutine { continuation ->
        Common.createGrpcChannel(app.id, Constants.Common.DEFAULT_PORT).also { channel ->
            ApplicationGrpc.newStub(channel)
                .withDeadlineAfter(20, TimeUnit.MINUTES)
                .clean(CleanRequest.newBuilder().build(), object : StreamObserver<CleanResponse> {
                    override fun onNext(value: CleanResponse?) {
                        continuation.resume(true)
                    }

                    override fun onError(t: Throwable?) {
                        continuation.resumeWithException(t ?: Exception("Unable to clean app"))
                    }

                    override fun onCompleted() {
                        channel.shutdown()
                    }
                })
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

    suspend fun createImage(file: File, tag: String) = suspendCancellableCoroutine<Unit> { continuation ->
        dockerClient.listImagesCmd().apply {
            filters?.put("reference", listOf(tag))
        }.exec().firstOrNull() ?: dockerClient.buildImageCmd(
                file
            )
            .withTags(setOf(tag))
            .withQuiet(false)
            .exec(object: BuildImageResultCallback() {
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
        continuation.resume(Unit)
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
                .exec(object : ResultCallback.Adapter<Frame>() {
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
            val callback = object : ResultCallback.Adapter<Event>() {
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
        env: List<String>? = null, systemCtls: Map<String, String>? = null
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
                                if(!systemCtls.isNullOrEmpty()) {
                                    withSysctls(systemCtls)
                                    withPrivileged(true)
                                }
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