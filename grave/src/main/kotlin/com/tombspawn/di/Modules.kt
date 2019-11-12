package com.tombspawn.di

import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.netty.NettyDockerCmdExecFactory
import com.tombspawn.data.Database
import com.tombspawn.data.StringMap
import com.tombspawn.git.CredentialProvider
import com.tombspawn.base.common.Command
import com.tombspawn.base.common.CommandResponse
import com.tombspawn.base.config.JsonApplicationConfig
import com.tombspawn.models.config.*
import com.tombspawn.network.GradleExecutor
import com.tombspawn.network.docker.DockerApiClient
import com.tombspawn.slackbot.SlackClient
import com.tombspawn.utils.Constants
import io.ktor.application.Application
import io.ktor.http.URLProtocol
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module
import redis.clients.jedis.Protocol

val gradleBotClient = module {
    factory { (application: Application, appDir: String, responseListener: MutableMap<String, CompletableDeferred<CommandResponse>>,
                  requestExecutor: SendChannel<Command>) ->
        GradleExecutor(
            appDir,
            get<Common> {
                parametersOf(application)
            }.gradlePath,
            responseListener,
            requestExecutor,
            get {
                parametersOf(application)
            }
        )
    }
}