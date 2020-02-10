package com.tombspawn.common

import io.ktor.http.Url
import io.ktor.http.fullPath
import io.ktor.http.hostWithPort
import org.mockito.Mockito

private val Url.hostWithPortIfRequired: String get() = if (port == protocol.defaultPort) host else hostWithPort
private val Url.fullUrl: String get() = "${protocol.name}://$hostWithPortIfRequired$fullPath"

fun <T> anyObject(): T {
    Mockito.anyObject<T>()
    return uninitialized()
}

fun <T> uninitialized(): T = null as T