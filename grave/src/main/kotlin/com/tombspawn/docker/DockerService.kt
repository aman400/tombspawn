package com.tombspawn.docker

import com.github.dockerjava.api.model.*
import com.google.gson.Gson
import com.tombspawn.base.common.CommonConstants
import com.tombspawn.base.common.exhaustive
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.di.qualifiers.Debuggable
import com.tombspawn.git.CredentialProvider
import com.tombspawn.models.AppContainerRequest
import com.tombspawn.models.config.App
import com.tombspawn.models.config.Common
import com.tombspawn.models.config.ServerConf
import com.tombspawn.utils.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import org.slf4j.LoggerFactory
import java.io.*
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject

@AppScope
class DockerService @Inject constructor(
    private val dockerClient: DockerApiClient,
    private val common: Common,
    private val credentialProvider: CredentialProvider,
    private val gson: Gson,
    @Debuggable
    private val debug: Boolean
) {

    private val LOGGER = LoggerFactory.getLogger("com.tombspawn.docker.DockerService")

    private val containerMapping = mutableMapOf<String, ContainerInfo>()

    private lateinit var sendChannel: SendChannel<QueueAction>
    private lateinit var processCommandChannel: SendChannel<ProcessRequestCommand>

    fun killContainer(app: App) {
        dockerClient.stopContainer(app.id)
        dockerClient.killContainer(app.id)
    }

    private fun CoroutineScope.processRequest(): SendChannel<ProcessRequestCommand> =
        actor(capacity = Channel.UNLIMITED) {
            for (msg in channel) {
                launch(Dispatchers.IO) {
                    msg.deferred.invoke()
                }
            }
        }

    private fun CoroutineScope.queueAction(): SendChannel<QueueAction> = actor(capacity = Channel.UNLIMITED) {
        for (msg in channel) {
            when (msg) {
                is QueueAddAction -> {
                    LOGGER.trace("Adding entry")
                    if (containerMapping[msg.id] == null) {
                        containerMapping[msg.id] = ContainerInfo(msg.id)
                    }
                    containerMapping[msg.id]?.addNewTask(msg.deferred)
                }
                is QueueProcessCompleteAction -> {
                    LOGGER.trace("Process completed with id ${msg.id}")
                    containerMapping[msg.id]?.onTaskCompleted()
                }
                is QueueVerifyAndRunAction -> {
                    if (getRunningTasks() < common.parallelThreads) {
                        val nextEntry = getNextTask()
                        LOGGER.trace("NextEntry $nextEntry")
                        val next = nextEntry?.value?.getNextTask()
                        if (next != null) {
                            processCommandChannel.send(ProcessRequestCommand(nextEntry.key, next))
                        } else {
                            LOGGER.trace("Waiting for next queue execution")
                        }
                    } else {
                        LOGGER.trace("Waiting for next queue execution")
                    }
                }
            }.exhaustive
        }
    }

    suspend fun startChannels() = coroutineScope {
        sendChannel = queueAction()
        processCommandChannel = processRequest()
    }

    suspend fun logEvents() = coroutineScope {
        dockerClient.logEvents { event ->
            if (event.type == EventType.CONTAINER) {
                when (Status.from(event.status)) {
                    Status.STOP -> {
                        containerMapping.filter { entry ->
                            entry.value.containerId == event.id
                        }.forEach { entry ->
                            GlobalScope.launch(Dispatchers.Default) {
                                entry.value.setContainerState(ContainerState.DEAD)
                                entry.value.resetTasks()
                            }
                        }
                    }
                    Status.EXEC_DIE -> {
                        containerMapping.filter { entry ->
                            entry.value.containerId == event.id
                        }.forEach { entry ->
                            GlobalScope.launch(Dispatchers.Default) {
                                dockerClient.restartContainer(entry.key)
                            }
                        }
                    }
                    else -> {
                        LOGGER.trace(event.toString())
                    }
                }
            } else {
                LOGGER.trace(event.toString())
            }
        }
    }

    suspend fun createContainer(app: App, port: Int, callbackUri: String) =
        coroutineScope {
            LOGGER.debug("${System.getProperty("user.dir")}/skeleton/Dockerfile")
            dockerClient.createImage(
                File("${System.getProperty("user.dir")}/skeleton/AndroidDockerfile"),
                "android-sdk"
            )
            File("${System.getProperty("user.dir")}/skeleton/Dockerfile").takeIf {
                it.exists()
            }?.let {
                dockerClient.createImage(it, "skeleton")
            }
            try {
                File("${System.getProperty("user.dir")}/skeleton/apps/${app.id}/Dockerfile").let {
                    // Clear older Dockerfile if any
                    if(it.exists()) {
                        it.delete()
                    }
                    // Create all parent directories
                    it.parentFile.mkdirs()
                    @Suppress("BlockingMethodInNonBlockingContext")
                    // Create Dockerfile for given app
                    it.createNewFile()
                    // Copy given files to given directories
                    val dataToAppend = "FROM skeleton as ${app.id}" + app.fileMappings?.joinToString("") { mapping ->
                        "\nCOPY ${mapping.name} ${mapping.path}"
                    }
                    @Suppress("BlockingMethodInNonBlockingContext")
                    FileWriter(it).use { writer ->
                        writer.write(dataToAppend)
                    }
                    it
                }.let {
                    dockerClient.createImage(it, app.id)
                }
            } catch (exception: Exception) {
                LOGGER.error("Unable to create image for $app", exception)
            }
            // Create network for docker
            val networkId = dockerClient.createNetwork("tombspawn")
//            val gradle = dockerClient.createVolume("gradle")
            // Create android cache volume
            val android = dockerClient.createVolume("android")
//            val gradleCache = Bind(gradle, Volume("/home/skeleton/.gradle/caches/"))
            val androidCache = Bind(android, Volume("/home/skeleton/.android/"))
            // volume for cloned apps to persist them
            val gitApps = dockerClient.createVolume("git")
            val appVolumeBind = Bind(gitApps, Volume("/app/"))

            val appPath = "/app/${app.id}/"
            app.dir = appPath

            val request = gson.toJson(
                AppContainerRequest(
                    ServerConf("http", "0.0.0.0", Constants.Common.DEFAULT_PORT, debug),
                    app, common, credentialProvider, callbackUri
                ), AppContainerRequest::class.java
            )

            val exposedPort = ExposedPort.tcp(Constants.Common.DEFAULT_PORT)
            val portBindings = Ports()
            portBindings.bind(exposedPort, Ports.Binding.bindPort(port))
            dockerClient.createContainer(
                app.id,
                app.id, listOf(
                    "java",
                    "-server",
                    "-XX:+UnlockExperimentalVMOptions",
                    "-XX:+UseCGroupMemoryLimitForHeap",
                    "-XX:InitialRAMFraction=2",
                    "-XX:MinRAMFraction=2",
                    "-XX:MaxRAMFraction=2",
                    "-XX:+UseG1GC",
                    "-XX:MaxGCPauseMillis=100",
                    "-XX:+UseStringDeduplication",
                    "-jar",
                    "application.jar",
                    request,
                    "--verbose"
                ), null, listOf(androidCache, appVolumeBind),
                portBindings, listOf(exposedPort), app.memory, app.swap, app.cpuShares, app.env
            )?.let { containerId ->
                if (containerMapping[app.id] == null) {
                    containerMapping[app.id] = ContainerInfo(app.id, containerId, ContainerState.CREATED)
                } else {
                    containerMapping[app.id]?.containerId = containerId
                }
                dockerClient.logContainerCommand(app.id, containerId)
                dockerClient.attachNetwork(containerId, networkId)
                dockerClient.startContainer(containerId)
            }
            LOGGER.debug("Created ${app.name}")
            Unit
        }

    suspend fun appInitialized(app: App) = coroutineScope {
        containerMapping.filter { entry ->
            entry.value.appId == app.id
        }.forEach { entry ->
            entry.value.setContainerState(ContainerState.STARTED)
        }
    }

    suspend fun fetchReferences(app: App, callbackUri: String) {
        sendChannel.offer(QueueAddAction(app.id) {
            dockerClient.fetchReferences(app, callbackUri)
        })
    }

    suspend fun cleanApp(app: App, callbackUri: String) {
        sendChannel.offer(QueueAddAction(app.id) {
            dockerClient.cleanApp(app, callbackUri)
        })
    }

    suspend fun fetchBuildVariants(app: App, callbackUri: String) {
        sendChannel.offer(QueueAddAction(app.id) {
            dockerClient.fetchBuildVariants(app, callbackUri)
        })
    }

    suspend fun fetchFlavours(app: App, callbackUri: String) {
        sendChannel.offer(QueueAddAction(app.id) {
            dockerClient.fetchFlavours(app, callbackUri)
        })
    }

    suspend fun generateApp(
        appId: String, successCallbackUri: String, failureCallbackUri: String,
        apkPrefix: String, buildData: Map<String, String>
    ) {
        sendChannel.offer(QueueAddAction(appId) {
            dockerClient.generateApp(
                appId, CommonConstants.APP_PREFIX to listOf(apkPrefix),
                CommonConstants.SUCCESS_CALLBACK_URI to listOf(successCallbackUri),
                CommonConstants.FAILURE_CALLBACK_URI to listOf(failureCallbackUri), *buildData.map {
                    Pair(it.key, listOf(it.value))
                }.toTypedArray()
            )
        })
    }

    fun onTaskCompleted(id: String) {
        sendChannel.offer(QueueProcessCompleteAction(id))
    }

    private fun getRunningTasks(): Int {
        LOGGER.trace(containerMapping.toString())
        return containerMapping.map {
            it.value.getRunningTaskCount()
        }.takeIf { it.isNotEmpty() }?.reduce { acc, i -> acc + i } ?: 0
    }

    private fun getNextTask(): Map.Entry<String, ContainerInfo>? {
        return containerMapping.filter {
            it.value.canRunTasks()
        }.entries.shuffled().firstOrNull()
    }

    @UseExperimental(InternalCoroutinesApi::class)
    suspend fun startQueueExecution() {
        while (true) {
            delay(3000)
            if (::sendChannel.isInitialized) {
                sendChannel.offer(QueueVerifyAndRunAction)
            }
        }
    }
}