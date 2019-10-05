package com.tombspawn.di

import com.tombspawn.auth.JWTConfig
import com.tombspawn.data.Database
import com.tombspawn.data.StringMap
import com.tombspawn.extensions.commandExecutor
import com.tombspawn.git.CredentialProvider
import com.tombspawn.base.common.Command
import com.tombspawn.base.common.CommandResponse
import com.tombspawn.models.config.*
import com.tombspawn.network.GradleExecutor
import com.tombspawn.slackbot.SlackClient
import com.tombspawn.utils.Constants
import io.ktor.application.Application
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

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
            get(),
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
        redis.clients.jedis.Jedis(redisConfig.host, redisConfig.port, 100000)
    }

    single { (application: Application) ->
        StringMap(
            get {
                parametersOf(application)
            }
        )
    }
}

val authentication = module {
    single { (secret: String, issuer: String, audience: String) ->
        JWTConfig(secret, issuer, audience)
    }
}

@UseExperimental(KtorExperimentalAPI::class)
val config = module {
    factory { (application: Application, coroutineScope: CoroutineScope) ->
        application.environment.config.configList("conf.apps").map {
            val responseListener = mutableMapOf<String, CompletableDeferred<CommandResponse>>()
            val requestExecutor = coroutineScope.commandExecutor(responseListener)
            val directory = it.propertyOrNull("dir")?.getString()
            App(
                it.property("id").getString(),
                it.propertyOrNull("name")?.getString(),
                it.propertyOrNull("app_url")?.getString(),
                it.propertyOrNull("repo_id")?.getString(),
                directory,
                it.property("remote_uri").getString(),
                get {
                    parametersOf(application, directory, responseListener, requestExecutor)
                } as GradleExecutor
            )
        }
    }

    single { (application: Application) ->
        application.environment.config.config("conf.slack").let {
            Slack(
                it.property("token").getString(), it.property("auth_token").getString(),
                it.property("client_id").getString(), it.property("secret").getString()
            )
        }
    }

    single { (application: Application) ->
        application.environment.config.config("conf.db").let {
            Db(
                it.property("url").getString(),
                it.property("username").getString(),
                it.property("password").getString()
            )
        }
    }

    single { (application: Application) ->
        application.environment.config.config("conf.jwt").let {
            JWT(
                it.property("domain").getString(),
                it.property("audience").getString(),
                it.property("realm").getString(),
                it.property("secret").getString()
            )
        }
    }

    single { (application: Application) ->
        application.environment.config.config("conf.common").let {
            Common(
                it.property("base_url").getString(),
                it.property("gradle_path").getString()
            )
        }
    }

    single { (application: Application) ->
        application.environment.config.config("conf.redis").let {
            Redis(
                it.propertyOrNull("host")?.getString() ?: Constants.Common.LOCALHOST,
                it.propertyOrNull("port")?.getString()?.toInt() ?: Constants.Common.DEFAULT_REDIS_PORT
            )
        }

    }

    single { (application: Application) ->
        application.environment.config.config("conf.git").let {
            CredentialProvider(
                it.propertyOrNull("ssh_file")?.getString(), it.propertyOrNull("passphrase")?.getString(),
                it.propertyOrNull("username")?.getString(), it.propertyOrNull("password")?.getString()
            )
        }
    }

    single(StringQualifier(Constants.EnvironmentVariables.ENV_UPLOAD_DIR_PATH)) {
        "${System.getProperty("user.dir")}/temp"
    }
}