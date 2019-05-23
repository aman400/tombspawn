package com.ramukaka.extensions

import com.google.gson.Gson
import com.ramukaka.network.CallError
import com.ramukaka.network.Failure
import com.ramukaka.network.Response
import com.ramukaka.network.Success
import io.ktor.client.call.HttpClientCall
import io.ktor.client.response.readBytes
import io.ktor.http.isSuccess
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend inline fun <reified T> HttpClientCall.await(): Response<T> = suspendCancellableCoroutine { continuation ->
    GlobalScope.launch(continuation.context) {
        try {
            when {
                response.status.isSuccess() -> {
                    val data = response.readBytes()
                    continuation.resume(Success(Gson().fromJson(data.toString(Charsets.UTF_8), T::class.java)))
                }

                response.status.value in 400..599 -> {
                    continuation.resume(Failure(response.readBytes().toString(Charsets.UTF_8), null))
                }
                else -> {
                    continuation.resume(CallError(Throwable(response.status.description)))
                }
            }
        } catch (exception: Exception) {
            continuation.resume(CallError(exception))
        }
    }
}