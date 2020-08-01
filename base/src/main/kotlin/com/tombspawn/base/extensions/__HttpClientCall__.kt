package com.tombspawn.base.extensions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tombspawn.base.common.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.isSuccess
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import kotlin.coroutines.resume

val NETWORK_LOGGER = LoggerFactory.getLogger("com.tombspawn.base.extensions.HttpClientCall")

suspend inline fun <reified T> HttpResponse.await(): Response<T> = suspendCancellableCoroutine { continuation ->
    GlobalScope.launch(continuation.context) {
        try {
            when {
                status.isSuccess() -> {
                    continuation.resume(
                        CallSuccess(
                            Gson().fromJson(
                                readText(Charsets.UTF_8),
                                object : TypeToken<T>() {}.type
                            )
                        )
                    )
                }
                status.value in 400..499 -> {
                    continuation.resume(CallFailure(readText(Charsets.UTF_8), null))
                }
                status.value in 500..599 -> {
                    continuation.resume(
                        ServerFailure(
                            Throwable(status.description), status.value,
                            "Something went wrong"
                        )
                    )
                }
                else -> {
                    continuation.resume(CallError(Throwable(status.description)))
                }
            }
        } catch (exception: Exception) {
            continuation.resume(CallError(exception))
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> HttpResponse.asFlow() = callbackFlow<Response<T>> {
    try {
        when {
            status.isSuccess() -> {
                try {
                    sendBlocking(
                        CallSuccess(
                            Gson().fromJson(
                                readText(Charsets.UTF_8),
                                object : TypeToken<T>() {}.type
                            )
                        )
                    )
                    close()
                } catch (exception: Exception) {
                    NETWORK_LOGGER.error("Unable to send data", exception)
                }
            }
            status.value in 400..499 -> {
                try {
                    sendBlocking(
                        CallFailure(
                            readText(Charsets.UTF_8), null, status.value
                        )
                    )
                    close()
                } catch (exception: Exception) {
                    NETWORK_LOGGER.error("Unable to send server error", exception)
                }
            }
            status.value in 500..599 -> {
                try {
                    sendBlocking(
                        ServerFailure(null, status.value, readText(Charsets.UTF_8))
                    )
                    close()
                } catch (exception: Exception) {
                    NETWORK_LOGGER.error("Unable to send server error", exception)
                }
            }
            else -> {
                try {
                    sendBlocking(CallError(Throwable(status.description)))
                    close()
                } catch (exception: Exception) {
                    NETWORK_LOGGER.error("Unable to send server error", exception)
                }
            }
        }
    } catch (exception: Exception) {
        try {
            sendBlocking(CallError(Throwable(status.description)))
            close()
        } catch (exception: Exception) {
            NETWORK_LOGGER.error("Unable to send server error", exception)
        }
    }

    awaitClose {
        this@asFlow.cancel()
    }
}