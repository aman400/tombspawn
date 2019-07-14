package com.ramukaka.extensions

import com.google.gson.Gson
import com.ramukaka.models.slack.generateCallback
import com.ramukaka.network.CallError
import com.ramukaka.network.CallFailure
import com.ramukaka.network.Response
import com.ramukaka.network.CallSuccess
import io.ktor.client.call.HttpClientCall
import io.ktor.client.response.readBytes
import io.ktor.client.response.readText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.io.copyAndClose
import kotlinx.coroutines.io.readUTF8Line
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend inline fun <reified T> HttpClientCall.await(): Response<T> = suspendCancellableCoroutine { continuation ->
    GlobalScope.launch(continuation.context) {
        try {
            when {
                response.status.isSuccess() -> {
                    continuation.resume(CallSuccess(Gson().fromJson(response.readText(Charsets.UTF_8), T::class.java)))
                }

                response.status.value in 400..599 -> {
                    continuation.resume(CallFailure(response.readText(Charsets.UTF_8), null))
                }
                else -> {
                    continuation.resume(CallError(Throwable(response.status.description)))
                }
            }
        } catch (exception: Exception) {
            continuation.resume(CallError(exception))
        } finally {
            close()
        }
    }


}