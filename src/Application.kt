package com.ramukaka

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
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.url
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
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
//        get<MyLocation> {
//            call.respondText("Location: name=${it.name}, arg1=${it.arg1}, arg2=${it.arg2}")
//        }
//        // Register nested routes
//        get<Type.Edit> {
//            call.respondText("Inside $it")
//        }
//        get<Type.List> {
//            call.respondText("Inside $it")
//        }

        post<App.Build> {
            launch {
                "cd /Users/aman/git/LazySocket && ./gradlew -q assembleWithArgs -Purl=abc.com -PfilePath=./public/ && cd - && rm -rf ./public && mv /Users/aman/git/LazySocket/app/public".runCommand()

                val file = File("/Users/aman/IdeaProjects/Ramukaka/public/App-debug.apk")
                if(file.exists()) {
                    println("File exists with length: ${file.length()}")
                }
                val api = ServiceGenerator.createService(RamukakaApi::class.java)
                val call = api.get(1)
                val response = call.execute()
                if(response.isSuccessful) {
                    println(response.body().toString())
                } else {
                    println(response.errorBody().toString())
                }
            }
            call.respond(it.url)
        }

        get<App.Get> {
            call.respond(it.id)
        }

        post<App> {
            if (!call.request.isMultipart()) {
                call.receiveMultipart()
                call.respondTextWriter {
                    appendln("This is a multipart request")
                }
            } else {
                call.respond("This is not a multipart request")
            }
        }

        get("/") {
            call.respondText { "HELLO WORLD!" }
        }
//
//        get("/json/gson") {
//            call.respond(mapOf("hello" to "world"))
//        }
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

@Location("/location/{name}")
class MyLocation(val name: String, val arg1: Int = 42, val arg2: String = "default")

@Location("/type/{name}")
data class Type(val name: String) {
    @Location("/edit")
    data class Edit(val type: Type)

    @Location("/list/{page}")
    data class List(val type: Type, val page: Int)
}

