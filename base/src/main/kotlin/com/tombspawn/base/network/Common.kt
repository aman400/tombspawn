package com.tombspawn.base.network

import com.tombspawn.base.di.Constants
import com.tombspawn.base.di.LOGGER
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.http.URLProtocol

object Common {
    @JvmStatic
    @JvmOverloads
    fun createHttpClient(gsonSerializer: GsonSerializer, hostName: String? = null,
                         scheme: URLProtocol? = URLProtocol.HTTPS, startPath: String? = null,
                         connectionTimeout: Int = 60_000, socketConnectionTimeout: Int = 60_000,
                         requestTimeout: Int = 20_000, enableLogger: Boolean = true): HttpClient {
        return HttpClient(Apache) {
            followRedirects = true
            engine {
                connectTimeout = connectionTimeout
                socketTimeout = socketConnectionTimeout
                connectionRequestTimeout = requestTimeout
            }
            install(Logging) {
                if(enableLogger) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            LOGGER.debug(message)
                        }
                    }
                    level = LogLevel.ALL
                } else {
                    level = LogLevel.NONE
                }
            }
            install(JsonFeature) {
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