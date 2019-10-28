package com.tombspawn.network.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallbackTemplate
import com.github.dockerjava.api.model.*
import com.github.dockerjava.core.command.BuildImageResultCallback
import com.github.dockerjava.core.command.EventsResultCallback
import com.github.dockerjava.core.command.LogContainerResultCallback
import com.tombspawn.di.docker
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.coroutines.suspendCoroutine

class DockerApiClient(private val dockerClient: DockerClient) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger("com.tombspawn.docker.DockerApiClient")
        const val STATE_STARTED = "running"
        const val STATE_CREATED = "created"
        const val STATE_RESTARTING = "restarting"
        const val STATE_PAUSED = "paused"
        const val STATE_EXITED = "exited"
        const val STATE_DEAD = "dead"
    }

    suspend fun listContainer(): MutableList<Container>? {
        return suspendCoroutine {
            dockerClient.listContainersCmd()
                .withShowSize(true)
                .withShowAll(true)
                .withStatusFilter(listOf("exited")).exec()
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

    @Suppress("BlockingMethodInNonBlockingContext")
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
                                println(it)
                            }
                            it.errorDetail?.let {
                                println("${it.code} : ${it.message}")
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
                            println(String(it))
                        }
                    }
                })
        }
    }

    suspend fun logEvents() {
        coroutineScope {
            val callback = object : EventsResultCallback() {
                override fun onNext(event: Event) {
                    println("Event: $event")
                    super.onNext(event)
                }
            }
            dockerClient.eventsCmd().exec(callback)
            println("Finished execution")
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
                            println(message)
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
                            println("Container ${it.state}")
                        }
                    }
                }
        }
    }
}