package com.tombspawn.network

import com.tombspawn.base.common.CallError
import com.tombspawn.base.common.CallFailure
import com.tombspawn.base.common.CallSuccess
import com.tombspawn.base.common.exhaustive
import com.tombspawn.data.Database
import com.tombspawn.base.extensions.await
import com.tombspawn.base.extensions.copyToSuspend
import com.tombspawn.utils.Constants
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.sessions.sessions
import kotlinx.coroutines.coroutineScope
import com.tombspawn.models.slack.BotInfo
import java.io.File


fun Routing.status() {
    get("/") {
        println(call.sessions.get(Constants.Slack.SESSION))

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


@Throws(Exception::class)
suspend fun fetchBotData(client: HttpClient, database: Database, botToken: String) = coroutineScope {
    val call = client.call {
        method = HttpMethod.Get
        url {
            encodedPath = "/api/rtm.connect"
            parameter("token", botToken)
        }
    }

    when(val response = call.await<BotInfo>()) {
        is CallSuccess -> {
            response.data?.let { botInfo ->
                if (botInfo.ok) {
                    botInfo.self?.let { about ->
                        database.addUser(about.id!!, about.name, typeString = Constants.Database.USER_TYPE_BOT)
                    }
                }
            }
        }
        is CallFailure -> {
            println(response.errorBody)
        }
        is CallError -> {
            response.throwable?.printStackTrace()
        }
    }.exhaustive
}