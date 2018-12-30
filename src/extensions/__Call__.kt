package com.ramukaka.extensions

import com.ramukaka.network.CallError
import com.ramukaka.network.Failure
import com.ramukaka.network.RetrofitResponse
import com.ramukaka.network.Success
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume

suspend fun <T> Call<T>.await(): RetrofitResponse<T> = suspendCancellableCoroutine { continuation ->
    this.enqueue(object : Callback<T> {
        override fun onFailure(call: Call<T>, t: Throwable) {
            continuation.resume(CallError(t))
        }

        override fun onResponse(call: Call<T>, response: Response<T>) {
            if(response.isSuccessful) {
                continuation.resume(Success(response.body()))
            } else {
                continuation.resume(Failure(response.errorBody()?.charStream().use {
                    it?.readText()
                }))
            }
        }
    })

    continuation.invokeOnCancellation {
        cancel()
    }
}