package com.tombspawn.common

import io.ktor.http.Url
import io.ktor.http.fullPath
import io.ktor.http.hostWithPort

private val Url.hostWithPortIfRequired: String get() = if (port == protocol.defaultPort) host else hostWithPort
private val Url.fullUrl: String get() = "${protocol.name}://$hostWithPortIfRequired$fullPath"