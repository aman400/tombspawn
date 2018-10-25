package com.ramukaka

import com.ramukaka.models.database.Users
import com.ramukaka.network.githubWebhook
import com.ramukaka.network.interceptors.LoggingInterceptor
import com.ramukaka.network.slackAction
import com.ramukaka.network.slackEvent
import com.ramukaka.network.subscribe
import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.locations.Location
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.routing.routing
import io.reactivex.disposables.CompositeDisposable
import network.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.event.Level
import java.io.File
import java.util.logging.Logger

fun main(args: Array<String>): Unit = io.ktor.server.netty.DevelopmentEngine.main(args)

private var UPLOAD_DIR_PATH = "${System.getProperty("user.dir")}/temp"
private var GRADLE_PATH = System.getenv()["GRADLE_PATH"]!!
private var CONSUMER_APP_DIR = System.getenv()["CONSUMER_APP_DIR"]!!
private var FLEET_APP_DIR = System.getenv()["FLEET_APP_DIR"]!!
private var TOKEN = System.getenv()["SLACK_TOKEN"]!!
private var O_AUTH_TOKEN = System.getenv()["O_AUTH_TOKEN"]!!
private var DB_URL = System.getenv()["DB_URL"]!!
private var DB_USER = System.getenv()["DB_USER"]!!
private var DB_PASSWORD = System.getenv()["DB_PASSWORD"]!!

@Suppress("unused") // Referenced in application.conf
fun Application.module() {

    Database.connect(
        url = DB_URL,
        driver = "com.mysql.cj.jdbc.Driver",
        password = DB_PASSWORD,
        user = DB_USER
    )


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

    transaction {
        SchemaUtils.create(Users)
    }

    HttpClient(OkHttp) {
        engine {
            config {
                followRedirects(true)
            }

            addInterceptor(LoggingInterceptor())
        }
    }

    intercept(ApplicationCallPipeline.Monitoring) {

        if (!call.parameters.isEmpty()) {
            println("Parameters: ")
            call.parameters.forEach { key, valuesList ->
                println("$key: ${valuesList.joinToString(",")}")
            }
        }

        if (!call.request.headers.isEmpty()) {
            println("Headers:")
            call.request.headers.forEach { key, valuesList ->
                println("$key: ${valuesList.joinToString(",")}")
            }
        }

        if (!call.request.queryParameters.isEmpty()) {
            println("Query Params: ")
            call.request.queryParameters.forEach { key, valuesList ->
                println("$key: ${valuesList.joinToString(",")}")
            }
        }

    }

    routing {
        status()
        health()
        buildConsumer(GRADLE_PATH, UPLOAD_DIR_PATH, CONSUMER_APP_DIR, TOKEN)
        buildFleet(GRADLE_PATH, UPLOAD_DIR_PATH, FLEET_APP_DIR, TOKEN)
        receiveApk(UPLOAD_DIR_PATH)
        slackEvent(O_AUTH_TOKEN)
        subscribe()
        slackAction(O_AUTH_TOKEN, CONSUMER_APP_DIR, GRADLE_PATH)
        githubWebhook()
    }
}

@Location("/app")
data class Apk(val file: File)


