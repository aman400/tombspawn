package com.ramukaka.di

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.ramukaka.annotations.DoNotDeserialize
import com.ramukaka.annotations.DoNotSerialize
import com.ramukaka.data.Database
import com.ramukaka.models.Command
import com.ramukaka.models.CommandResponse
import com.ramukaka.network.GradleBotClient
import com.ramukaka.network.SlackClient
import com.ramukaka.network.utils.Headers
import com.ramukaka.utils.Constants
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.JsonSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.observer.ResponseObserver
import io.ktor.client.request.headers
import io.ktor.http.HeadersBuilder
import io.ktor.http.URLProtocol
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

private const val ARG_GSON_BUILDER = "gson_builder"
private const val ARG_JSON_SERIALIZER = "json_serializer"

val dbModule = module {
    single {
        val dbUrl = System.getenv()["DB_URL"]!!
        val dbUser = System.getenv()["DB_USER"]!!
        val dbPassword = System.getenv()["DB_PASSWORD"]!!
        Database(dbUrl, dbUser, dbPassword)
    }
}

val gson = module {
    single(StringQualifier(ARG_GSON_BUILDER)) {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.setPrettyPrinting()
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
        HeadersBuilder().apply {
            append(Headers.APP_CLIENT, Headers.APP_CLIENT_VALUE)
            append(Headers.CONTENT_TYPE, Headers.TYPE_JSON)
            append(Headers.ACCEPT, Headers.TYPE_JSON)
        }
    }

    single {
        val headersBuilder: HeadersBuilder = get()

        HttpClient(Apache) {
            engine {
            }
            install(ResponseObserver) {

            }
            install(io.ktor.client.features.logging.Logging) {
                logger = object: Logger {
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
        }.config {
            defaultRequest {
                headers { headersBuilder.build() }
                url {
                    protocol = URLProtocol.HTTPS
                    host = "slack.com"
                }
            }
        }
    }
}

val slackModule = module {
    single { (responseListener: MutableMap<String, CompletableDeferred<CommandResponse>>, requestExecutor: SendChannel<Command>) ->
        SlackClient(
            get(StringQualifier(Constants.EnvironmentVariables.ENV_O_AUTH_TOKEN)),
            get(StringQualifier(Constants.EnvironmentVariables.ENV_GRADLE_PATH)),
            get(StringQualifier(Constants.EnvironmentVariables.ENV_UPLOAD_DIR_PATH)),
            get(),
            get(),
            get(StringQualifier(Constants.EnvironmentVariables.ENV_SLACK_TOKEN)),
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

    single(StringQualifier(Constants.EnvironmentVariables.ENV_UPLOAD_DIR_PATH)) {
        "${System.getProperty("user.dir")}/temp"
    }
}