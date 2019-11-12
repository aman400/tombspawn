package com.tombspawn.base.extensions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tombspawn.base.common.CallError
import com.tombspawn.base.common.CallFailure
import com.tombspawn.base.common.CallSuccess
import com.tombspawn.base.common.Response
import io.ktor.client.call.HttpClientCall
import io.ktor.client.response.readText
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
                    continuation.resume(CallSuccess(Gson().fromJson(response.readText(Charsets.UTF_8), object: TypeToken<T>() {}.type)))
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