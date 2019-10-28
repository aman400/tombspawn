package com.tombspawn.docker

import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.ExposedPort.tcp
import com.github.dockerjava.api.model.Ports
import com.github.dockerjava.api.model.Volume
import com.google.gson.Gson
import com.tombspawn.git.CredentialProvider
import com.tombspawn.models.AppContainerRequest
import com.tombspawn.models.config.App
import com.tombspawn.models.config.Common
import com.tombspawn.network.docker.DockerApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.io.File


suspend fun createContainers(
    dockerClient: DockerApiClient,
    apps: List<App>,
    common: Common,
    credentialProvider: CredentialProvider,
    gson: Gson
) =
    coroutineScope {
        dockerClient.createImage(File("/Users/aman/git/ramukaka/skeleton/DockerFile"), "skeleton")
        val networkId = dockerClient.createNetwork("tombspawn")
        val gradle = dockerClient.createVolume("gradle")
        val android = dockerClient.createVolume("android")
        val gradleBind = Bind(gradle, Volume("/home/skeleton/.gradle/"))
        val androidBind = Bind(android, Volume("/home/skeleton/.android/"))
        val gitApps = dockerClient.createVolume("git")
        val appVolumeBind = Bind(gitApps, Volume("/app/git/"))

        launch(Dispatchers.IO) {
            dockerClient.logEvents()
            println("Finished events")
        }
        apps.forEachIndexed { index, app ->
            val appPath = "/app/git/${app.id}/"
            app.dir = appPath

            val request =
                gson.toJson(AppContainerRequest(app, common, credentialProvider), AppContainerRequest::class.java)

            val exposedPort = tcp(8080)
            val portBindings = Ports()
            portBindings.bind(exposedPort, Ports.Binding.bindPort(common.basePort + index))
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
                ), null, listOf(gradleBind, androidBind, appVolumeBind),
                portBindings, listOf(exposedPort)
            )?.let { containerId ->
                dockerClient.logContainerCommand(app.id, containerId)
                dockerClient.attachNetwork(containerId, networkId)
                dockerClient.startContainer(containerId)
            }
            LoggerFactory.getLogger("Container").debug("Created ${app.name}")
            Unit
        }
    }