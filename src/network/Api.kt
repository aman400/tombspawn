package network

import com.google.gson.Gson
import com.ramukaka.data.Database
import com.ramukaka.extensions.copyToSuspend
import com.ramukaka.utils.Constants
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.parameter
import io.ktor.client.response.readBytes
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
import kotlinx.coroutines.coroutineScope
import models.slack.BotInfo
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


fun Routing.receiveApk(uploadDirPath: String) {
    post<Apk> {
        val multipart = call.receiveMultipart()
        var description: String
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    if (part.name == "description") {
                        description = part.value
                        println(description)
                    }
                }
                is PartData.FileItem -> {
                    val ext = File(part.originalFileName).extension
                    val name = File(part.originalFileName).name
                    val file = File(uploadDirPath, "upload-app.$ext")
                    part.streamProvider().use { input ->
                        file.outputStream().buffered().use { output ->
                            input.copyToSuspend(output)
                            call.respond(mapOf("message" to "Upload Complete"))
                        }
                    }
                }
            }

            part.dispose()
        }
    }
}

@Throws(Exception::class)
suspend fun fetchBotData(client: HttpClient, database: Database, botToken: String, gson: Gson) = coroutineScope {
    val data = client.call {
        method = HttpMethod.Get
        url {
            encodedPath = "/api/rtm.connect"
            parameter("token", botToken)
        }
    }.response.readBytes()

    val botInfo = gson.fromJson(data.toString(Charsets.UTF_8), BotInfo::class.java)

    botInfo.let {
        if (botInfo.ok) {
            it.self?.let { about ->
                database.addUser(about.id!!, about.name, typeString = Constants.Database.USER_TYPE_BOT)
            }
        }
    }
}