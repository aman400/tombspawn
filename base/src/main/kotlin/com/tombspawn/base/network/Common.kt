@file:JvmName("CommonUtils")

package com.tombspawn.base.network

import com.tombspawn.base.common.*
import com.tombspawn.base.di.Constants
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.http.URLProtocol
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger("com.tombspawn.base.network.CommonUtils")

object Common {
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

    fun createGrpcChannel(host: String, port: Int): ManagedChannel {
        return ManagedChannelBuilder
            .forAddress(host, port)
            .enableRetry()
            .usePlaintext()
            .build()
    }
}


suspend fun <T> withRetry(
    times: Int = 0,
    initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000,    // 1 second
    factor: Double = 2.0,
    block: suspend () -> Response<T>
): Response<T> {
    var currentDelay = initialDelay
    repeat(times) {
        when (val data = block()) {
            is CallSuccess -> {
                return data
            }
            is CallFailure -> {
                LOGGER.error("Call not completed ${data.errorCode}", data.throwable)
            }
            is ServerFailure -> {
                LOGGER.error("Call not completed ${data.errorCode}", data.throwable)
            }
            is CallError -> {
                LOGGER.error("Call not completed", data.throwable)
            }
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().apply {
            if(maxDelay >= 0) {
                coerceAtLeast(maxDelay)
            }
        }
    }
    return block() // last attempt
}