package network

import com.ramukaka.Apk
import com.ramukaka.data.Database
import com.ramukaka.extensions.copyToSuspend
import com.ramukaka.network.ServiceGenerator
import com.ramukaka.network.SlackClient
import com.ramukaka.utils.Constants
import io.ktor.application.call
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.locations.post
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
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
suspend fun fetchBotData(database: Database, botToken: String) = runBlocking {
    val api = ServiceGenerator.createService(
        SlackClient.SlackApi::class.java,
        SlackClient.SlackApi.BASE_URL,
        true,
        callAdapterFactory = RxJava2CallAdapterFactory.create()
    )
    val headers = mutableMapOf("Content-type" to "application/x-www-form-urlencoded")
    launch(coroutineContext) {
        val response = api.fetchBotInfo(headers, botToken).execute()
        if(response.isSuccessful) {
            val botInfo = response.body()
            botInfo?.let {
                if (botInfo.ok) {
                    runBlocking {
                        it.self?.let { about ->
                            database.addUser(about.id!!, about.name, typeString = Constants.Database.USER_TYPE_BOT)
                        }
                    }
                }
            }
        } else {
            response.errorBody()?.charStream()?.use {
                println(it.readText())
            }
        }
    }
}