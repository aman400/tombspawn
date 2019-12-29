package com.tombspawn.docker

import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.Ports
import com.github.dockerjava.api.model.Volume
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.tombspawn.base.common.CommonConstants
import com.tombspawn.base.common.Response
import com.tombspawn.di.qualifiers.Debuggable
import com.tombspawn.git.CredentialProvider
import com.tombspawn.models.AppContainerRequest
import com.tombspawn.models.config.App
import com.tombspawn.models.config.Common
import com.tombspawn.models.config.ServerConf
import com.tombspawn.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject

class DockerService @Inject constructor(
    private val dockerClient: DockerApiClient,
    private val common: Common,
    private val credentialProvider: CredentialProvider,
    private val gson: Gson,
    @Debuggable
    private val debug: Boolean
) {

    private val LOGGER = LoggerFactory.getLogger("com.tombspawn.docker.DockerService")

    suspend fun listContainers() = coroutineScope {
        dockerClient.listContainer()?.forEach {
            LOGGER.info(it.toString())
        }
    }

    fun killContainer(app: App) {
        dockerClient.stopContainer(app.id)
        dockerClient.killContainer(app.id)
    }

    suspend fun createContainer(app: App, port: Int, callbackUri: String) =
        coroutineScope {
            LOGGER.debug("${System.getProperty("user.dir")}/skeleton/Dockerfile")
            dockerClient.createImage(File("${System.getProperty("user.dir")}/skeleton/AndroidDockerfile"), "android-sdk")
            dockerClient.createImage(File("${System.getProperty("user.dir")}/skeleton/Dockerfile"), "skeleton")
            val networkId = dockerClient.createNetwork("tombspawn")
//            val gradle = dockerClient.createVolume("gradle")
            val android = dockerClient.createVolume("android")
//            val gradleCache = Bind(gradle, Volume("/home/skeleton/.gradle/caches/"))
            val androidCache = Bind(android, Volume("/home/skeleton/.android/"))
            val gitApps = dockerClient.createVolume("git")
            val appVolumeBind = Bind(gitApps, Volume("/app/git/"))

            launch(Dispatchers.IO) {
                dockerClient.logEvents()
            }

            val appPath = "/app/git/${app.id}/"
            app.dir = appPath

            val request = gson.toJson(AppContainerRequest(ServerConf("http", "0.0.0.0", Constants.Common.DEFAULT_PORT, debug),
                    app, common, credentialProvider, callbackUri), AppContainerRequest::class.java)

            val exposedPort = ExposedPort.tcp(Constants.Common.DEFAULT_PORT)
            val portBindings = Ports()
            portBindings.bind(exposedPort, Ports.Binding.bindPort(port))
            dockerClient.createContainer(
                "skeleton",
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
                portBindings, listOf(exposedPort)
            )?.let { containerId ->
                dockerClient.logContainerCommand(app.id, containerId)
                dockerClient.attachNetwork(containerId, networkId)
                dockerClient.startContainer(containerId)
            }
            LoggerFactory.getLogger("Container").debug("Created ${app.name}")
            Unit
        }

    suspend fun fetchReferences(app: App, callbackUri: String): JsonObject? {
        return dockerClient.fetchReferences(app, callbackUri)
    }

    suspend fun fetchBuildVariants(app: App, callbackUri: String): JsonObject? {
        return dockerClient.fetchBuildVariants(app, callbackUri)
    }

    suspend fun fetchFlavours(app: App, callbackUri: String): JsonObject? {
        return dockerClient.fetchFlavours(app, callbackUri)
    }

    suspend fun generateApp(
        appId: String, successCallbackUri: String, failureCallbackUri: String,
        apkPrefix: String, buildData: Map<String, String>
    ): Response<JsonObject> {
        return dockerClient.generateApp(appId,
            CommonConstants.APP_PREFIX to listOf(apkPrefix),
            CommonConstants.SUCCESS_CALLBACK_URI to listOf(successCallbackUri),
            CommonConstants.FAILURE_CALLBACK_URI to listOf(failureCallbackUri), *buildData.map {
                Pair(it.key, listOf(it.value))
            }.toTypedArray()
        )
    }
}