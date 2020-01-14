package com.tombspawn.base.extensions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tombspawn.base.common.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.isSuccess
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend inline fun <reified T> HttpResponse.await(): Response<T> = suspendCancellableCoroutine { continuation ->
    GlobalScope.launch(continuation.context) {
        try {
            when {
                status.isSuccess() -> {
                    continuation.resume(CallSuccess(Gson().fromJson(readText(Charsets.UTF_8),
                        object: TypeToken<T>() {}.type)))
                }
                status.value in 400..499 -> {
                    continuation.resume(CallFailure(readText(Charsets.UTF_8), null))
                }
                status.value in 500..599 -> {
                    continuation.resume(ServerFailure(Throwable(status.description), status.value,
                        "Something went wrong"))
                }
                else -> {
                    continuation.resume(CallError(Throwable(status.description)))
                }
            }
        } catch (exception: Exception) {
            continuation.resume(CallError(exception))
        } finally {
            call.client.close()
        }
    }
}