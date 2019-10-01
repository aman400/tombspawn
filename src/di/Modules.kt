package com.ramukaka.di

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.ramukaka.annotations.DoNotDeserialize
import com.ramukaka.annotations.DoNotSerialize
import com.ramukaka.auth.JWTConfig
import com.ramukaka.data.Database
import com.ramukaka.data.StringMap
import com.ramukaka.extensions.commandExecutor
import com.ramukaka.git.CredentialProvider
import com.ramukaka.models.Command
import com.ramukaka.models.CommandResponse
import com.ramukaka.models.config.*
import com.ramukaka.network.GradleExecutor
import com.ramukaka.network.utils.Headers
import com.ramukaka.slackbot.SlackClient
import com.ramukaka.utils.Constants
import io.ktor.application.Application
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.http.URLProtocol
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module
import org.redisson.Redisson
import org.redisson.config.Config
import org.redisson.config.TransportMode

private const val ARG_GSON_BUILDER = "gson_builder"
private const val ARG_JSON_SERIALIZER = "json_serializer"

val dbModule = module {
    single { (application: Application, isDebug: Boolean) ->
        val db = get<Db> {
            parametersOf(application)
        }
        Database(db.url, db.username, db.password, isDebug)
    }
}

val gsonModule = module {
    single(StringQualifier(ARG_GSON_BUILDER)) {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.setPrettyPrinting()
        gsonBuilder.serializeNulls()
        gsonBuilder.disableHtmlEscaping()
        gsonBuilder.enableComplexMapKeySerialization()
        gsonBuilder.addSerializationExclusionStrategy(object : ExclusionStrategy {
            override fun shouldSkipField(f: FieldAttributes): Boolean {
                return f.getAnnotation(DoNotSerialize::class.java) != null
            }

            override fun shouldSkipClass(clazz: Class<*>): Boolean {
                return clazz.getAnnotation(DoNotSerialize::class.java) != null
            }
        })
        gsonBuilder.addDeserializationExclusionStrategy(object : ExclusionStrategy {
            override fun shouldSkipField(f: FieldAttributes): Boolean {
                return f.getAnnotation(DoNotDeserialize::class.java) != null
            }

            override fun shouldSkipClass(clazz: Class<*>): Boolean {
                return clazz.getAnnotation(DoNotDeserialize::class.java) != null
            }
        })
        gsonBuilder
    }

    single(StringQualifier(ARG_JSON_SERIALIZER)) {
        GsonSerializer {
            get(StringQualifier(ARG_GSON_BUILDER)) as GsonBuilder
        }
    }

    single {
        (get(StringQualifier(ARG_GSON_BUILDER)) as GsonBuilder).create()
    }
}

val httpClientModule = module {
    single {
        HttpClient(Apache) {
            followRedirects = true
            engine {
                connectTimeout = 60_000
                socketTimeout = 60_000
                connectionRequestTimeout = 20_000
            }
            install(io.ktor.client.features.logging.Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println(message)
                    }
                }
                level = LogLevel.ALL
            }
            install(JsonFeature) {
                val gsonSerializer: GsonSerializer = get(StringQualifier(ARG_JSON_SERIALIZER))
                serializer = gsonSerializer
            }
            defaultRequest {
                headers.append(Headers.APP_CLIENT, Headers.APP_CLIENT_VALUE)
                url {
                    if (host == "localhost") {
                        protocol = URLProtocol.HTTPS
                        host = "slack.com"
                    }
                }
            }
        }
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

val redis = module {
    single { (application: Application) ->
        val redis = get<Redis> {
            parametersOf(application)
        }
        val config = Config()
        config.transportMode = TransportMode.NIO
        config.useSingleServer()
        .setTimeout(1000000)
        .setAddress("${redis.host}:${redis.port}")
        Redisson.create(config)
    }

    single { (application: Application, mapName: String) ->
        StringMap(
            get {
                parametersOf(application)
            },
            mapName
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
            Common(it.property("base_url").getString(),
                it.property("gradle_path").getString())
        }
    }

    single { (application: Application) ->
        application.environment.config.config("conf.redis").let {
            Redis(it.propertyOrNull("host")?.getString() ?: Constants.Common.LOCALHOST,
                    it.propertyOrNull("port")?.getString()?.toInt() ?: Constants.Common.DEFAULT_REDIS_PORT)
        }

    }

    single { (application: Application) ->
        application.environment.config.config("conf.git").let {
            CredentialProvider(it.propertyOrNull("ssh_file")?.getString(), it.propertyOrNull("passphrase")?.getString(),
                it.propertyOrNull("username")?.getString(), it.propertyOrNull("password")?.getString())
        }
    }

    single(StringQualifier(Constants.EnvironmentVariables.ENV_UPLOAD_DIR_PATH)) {
        "${System.getProperty("user.dir")}/temp"
    }
}