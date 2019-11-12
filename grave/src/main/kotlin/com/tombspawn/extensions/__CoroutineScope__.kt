package com.tombspawn.extensions

import com.tombspawn.base.common.*
import com.tombspawn.base.common.Response
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import java.io.IOException

suspend fun <T> retryCall(
    times: Int = 0,
    initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000,    // 1 second
    factor: Double = 2.0,
    block: suspend () -> Response<T>
): Response<T> {
    var currentDelay = initialDelay
    repeat(times) {
        val data = block()
        when (data) {
            is CallSuccess -> {
                return data
            }
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtLeast(maxDelay)
    }
    return block() // last attempt
}