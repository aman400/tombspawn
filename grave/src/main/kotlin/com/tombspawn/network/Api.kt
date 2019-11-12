package com.tombspawn.network

import com.tombspawn.base.common.CallError
import com.tombspawn.base.common.CallFailure
import com.tombspawn.base.common.CallSuccess
import com.tombspawn.base.common.exhaustive
import com.tombspawn.base.extensions.await
import com.tombspawn.data.Database
import com.tombspawn.models.slack.BotInfo
import com.tombspawn.utils.Constants
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import io.ktor.locations.Location
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import kotlinx.coroutines.coroutineScope
import java.io.File


fun Routing.status() {
    get("/") {
        call.respond(mapOf("status" to "OK"))
    }
}

fun Routing.health() {
    get("/health") {
        call.respond(mapOf("status" to "OK"))
    }
}

@Location("/app")
data class Apk(val file: File)