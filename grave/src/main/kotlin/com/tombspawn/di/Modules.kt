package com.tombspawn.di

import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.netty.NettyDockerCmdExecFactory
import com.tombspawn.data.Database
import com.tombspawn.data.StringMap
import com.tombspawn.extensions.commandExecutor
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
import io.ktor.client.HttpClient
import io.ktor.http.URLProtocol
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module
import redis.clients.jedis.Protocol

val dbModule = module {
    single { (application: Application, isDebug: Boolean) ->
        val db = get<Db> {
            parametersOf(application)
        }
        Database(db.url, db.username, db.password, isDebug)
    }
}

val slackModule = module {
    single { (application: Application) ->
        SlackClient(
            get {
                parametersOf("slack.com", URLProtocol.HTTPS, null)
            },
            get(StringQualifier(Constants.EnvironmentVariables.ENV_UPLOAD_DIR_PATH)),
            get(),
            get {
                parametersOf(application)
            },
            get()
        )
    }
}

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

val redisClient = module {
    single { (application: Application) ->
        val redisConfig = get<Redis> {
            parametersOf(application)
        }
        redis.clients.jedis.Jedis(
            redisConfig.host ?: Protocol.DEFAULT_HOST,
            redisConfig.port ?: Protocol.DEFAULT_PORT, 100000)
    }

    single { (application: Application) ->
        StringMap(
            get {
                parametersOf(application)
            }
        )
    }
}

val docker = module {
    single {
        val config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .build()

        DockerApiClient(
            DockerClientBuilder.getInstance(config).withDockerCmdExecFactory(
                NettyDockerCmdExecFactory()
                    .withConnectTimeout(60000)
                    .withReadTimeout(60000)
            ).build()
        )
    }
}

@UseExperimental(KtorExperimentalAPI::class)
val config = module {
    factory { (application: Application, coroutineScope: CoroutineScope) ->
        (application.environment.config as JsonApplicationConfig).configList("apps").map {
            val responseListener = mutableMapOf<String, CompletableDeferred<CommandResponse>>()
            val requestExecutor = coroutineScope.commandExecutor(responseListener)
            it.getAs(App::class.java).apply {
                gradleExecutor = get {
                    parametersOf(application, this.dir, responseListener, requestExecutor)
                } as GradleExecutor
                networkClient = get<HttpClient> {
                    parametersOf(this.appUrl, URLProtocol.HTTP, null)
                }
            }
        }
    }

    single { (application: Application) ->
        (application.environment.config as JsonApplicationConfig)
            .propertyOrNull("slack")
            ?.getAs(Slack::class.java)
    }

    single { (application: Application) ->
        (application.environment.config as JsonApplicationConfig)
            .propertyOrNull("db")
            ?.getAs(Db::class.java)
    }

    single { (application: Application) ->
        (application.environment.config as JsonApplicationConfig)
            .propertyOrNull("common")
            ?.getAs(Common::class.java)
    }

    single { (application: Application) ->
        (application.environment.config as JsonApplicationConfig)
            .propertyOrNull("redis")
            ?.getAs(Redis::class.java)
    }

    single { (application: Application) ->
        (application.environment.config as JsonApplicationConfig)
            .propertyOrNull("git")
            ?.getAs(CredentialProvider::class.java)
    }

    single(StringQualifier(Constants.EnvironmentVariables.ENV_UPLOAD_DIR_PATH)) {
        "${System.getProperty("user.dir")}/temp"
    }
}