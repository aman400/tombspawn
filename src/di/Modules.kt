package com.ramukaka.di

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.ramukaka.annotations.DoNotDeserialize
import com.ramukaka.annotations.DoNotSerialize
import com.ramukaka.auth.JWTConfig
import com.ramukaka.data.Database
import com.ramukaka.data.StringMap
import com.ramukaka.models.Command
import com.ramukaka.models.CommandResponse
import com.ramukaka.network.GradleBotClient
import com.ramukaka.network.utils.Headers
import com.ramukaka.slackbot.SlackClient
import com.ramukaka.utils.Constants
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.http.URLProtocol
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module
import org.redisson.Redisson
import org.redisson.config.Config
import org.redisson.config.TransportMode

private const val ARG_GSON_BUILDER = "gson_builder"
private const val ARG_JSON_SERIALIZER = "json_serializer"

val dbModule = module {
    single { (isDebug: Boolean) ->
        val dbUrl = System.getenv()["DB_URL"]!!
        val dbUser = System.getenv()["DB_USER"]!!
        val dbPassword = System.getenv()["DB_PASSWORD"]!!
        Database(dbUrl, dbUser, dbPassword, isDebug)
    }
}

val gson = module {
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
    single { (responseListener: MutableMap<String, CompletableDeferred<CommandResponse>>, requestExecutor: SendChannel<Command>) ->
        SlackClient(
            get(),
            get(StringQualifier(Constants.EnvironmentVariables.ENV_O_AUTH_TOKEN)),
            get(StringQualifier(Constants.EnvironmentVariables.ENV_GRADLE_PATH)),
            get(StringQualifier(Constants.EnvironmentVariables.ENV_UPLOAD_DIR_PATH)),
            get(),
            get(),
            get(StringQualifier(Constants.EnvironmentVariables.ENV_SLACK_TOKEN)),
            get(StringQualifier(Constants.EnvironmentVariables.ENV_SLACK_CLIENT_ID)),
            get(StringQualifier(Constants.EnvironmentVariables.ENV_SLACK_SECRET)),
            requestExecutor,
            responseListener
        )
    }
}

val gradleBotClient = module {
    single { (responseListener: MutableMap<String, CompletableDeferred<CommandResponse>>, requestExecutor: SendChannel<Command>) ->
        GradleBotClient(
            get(StringQualifier(Constants.EnvironmentVariables.ENV_GRADLE_PATH)),
            responseListener,
            requestExecutor
        )
    }
}

val redis = module {
    single {
        val config = Config()
        config.transportMode = TransportMode.NIO
        Redisson.create()
    }

    single { (mapName: String) ->
        StringMap(
            get(),
            mapName
        )
    }
}

val authentication = module {
    single { (secret: String, issuer: String, audience: String) ->
        JWTConfig(secret, issuer, audience)
    }
}

val envVariables = module {
    single(StringQualifier(Constants.EnvironmentVariables.ENV_O_AUTH_TOKEN)) {
        System.getenv()[Constants.EnvironmentVariables.ENV_O_AUTH_TOKEN]!!
    }

    single(StringQualifier(Constants.EnvironmentVariables.ENV_SLACK_TOKEN)) {
        System.getenv()[Constants.EnvironmentVariables.ENV_SLACK_TOKEN]!!
    }

    single(StringQualifier(Constants.EnvironmentVariables.ENV_GRADLE_PATH)) {
        Constants.Common.COMMAND_GRADLE
    }

    single(StringQualifier(Constants.EnvironmentVariables.ENV_SLACK_CLIENT_ID)) {
        System.getenv()[Constants.EnvironmentVariables.ENV_SLACK_CLIENT_ID]
    }

    single(StringQualifier(Constants.EnvironmentVariables.ENV_SLACK_SECRET)) {
        System.getenv()[Constants.EnvironmentVariables.ENV_SLACK_SECRET]
    }

    single(StringQualifier(Constants.EnvironmentVariables.ENV_UPLOAD_DIR_PATH)) {
        "${System.getProperty("user.dir")}/temp"
    }
}