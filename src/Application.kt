package com.ramukaka

import com.ramukaka.extensions.copyToSuspend
import com.ramukaka.extensions.runCommand
import com.ramukaka.network.RamukakaApi
import com.ramukaka.network.ServiceGenerator
import com.ramukaka.network.interceptors.LoggingInterceptor
import io.ktor.application.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.post
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.features.*
import org.slf4j.event.*
import io.ktor.gson.*
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.url
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.nio.charset.Charset

fun main(args: Array<String>): Unit = io.ktor.server.netty.DevelopmentEngine.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(Locations) {
    }

    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(DataConversion)

    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }

    routing {
        post<App.Build> {
            launch {
                "cd /Users/aman/git/LazySocket && ./gradlew -q assembleWithArgs -Purl=abc.com -PfilePath=./public/ && cd - && rm -rf ./public && mv /Users/aman/git/LazySocket/app/public".runCommand()

                val file = File("/Users/aman/IdeaProjects/Ramukaka/public/App-debug.apk")
                if (file.exists()) {
                    val requestBody = RequestBody.create(MediaType.parse(ServiceGenerator.MULTIPART_FORM_DATA), file)
                    val multipartBody = MultipartBody.Part.createFormData("Apk", "App-debug.apk", requestBody)

                    val descriptionString = "This is actual build file"
                    val description = RequestBody.create(
                        okhttp3.MultipartBody.FORM, descriptionString
                    )

                    val api = ServiceGenerator.createService(RamukakaApi::class.java)
                    val call = api.pushApp(description, multipartBody)
                    val response = call.execute()
                    if (response.isSuccessful) {
                        println(response.body()!!.message)
                    } else {
                        println(response.errorBody().toString())
                    }
                }

            }
            call.respond(it.url)
        }

        get<App.Get> {
            call.respond(it.id)
        }

        post<App> {
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
                        val file = File("/Users/aman/IdeaProjects/Ramukaka/public", "upload-app.$ext")
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

        get("/") {
            call.respondText { "HELLO WORLD!" }
        }
    }
}

data class Apk(val file: File)

@Location("/app")
class App {
    @Location("/build")
    data class Build(val url: String)

    @Location("/get/{id}")
    data class Get(val id: String)
}


