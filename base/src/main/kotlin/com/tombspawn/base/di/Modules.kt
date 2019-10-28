package com.tombspawn.base.di

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.tombspawn.base.annotations.DoNotDeserialize
import com.tombspawn.base.annotations.DoNotSerialize
import com.tombspawn.base.di.Constants.Di.ARG_GSON_BUILDER
import com.tombspawn.base.di.Constants.Di.ARG_JSON_SERIALIZER
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.http.URLProtocol
import org.koin.core.qualifier.StringQualifier
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.slf4j.LoggerFactory

val LOGGER = LoggerFactory.getLogger("com.tombspawn.base.Modules")

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
    factory { (hostName: String?, scheme: URLProtocol?, startPath: List<String>?) ->
        HttpClient(Apache) {
            followRedirects = true
            engine {
                connectTimeout = 60_000
                socketTimeout = 60_000
                connectionRequestTimeout = 20_000
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        LOGGER.debug(message)
                    }
                }
                level = LogLevel.ALL
            }
            install(JsonFeature) {
                val gsonSerializer: GsonSerializer = get(StringQualifier(ARG_JSON_SERIALIZER))
                serializer = gsonSerializer
            }
            defaultRequest {
                headers.append(Constants.Headers.APP_CLIENT, Constants.Headers.APP_CLIENT_VALUE)
                url {
                    if (host == "localhost" && !hostName.isNullOrBlank()) {
                        protocol = scheme ?: URLProtocol.HTTPS
                        host = hostName
                        startPath?.let {
                            path(it)
                        }
                    }
                }
            }
        }
    }
}