package com.ramukaka

import com.ramukaka.data.Database
import com.ramukaka.extensions.execute
import com.ramukaka.network.*
import com.ramukaka.network.interceptors.LoggingInterceptor
import com.ramukaka.utils.Constants
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import network.*
import org.slf4j.event.Level
import java.io.File

fun main(args: Array<String>): Unit = io.ktor.server.netty.DevelopmentEngine.main(args)

private var UPLOAD_DIR_PATH = "${System.getProperty("user.dir")}/temp"
private var GRADLE_PATH = System.getenv()["GRADLE_PATH"]!!
private var CONSUMER_APP_DIR = System.getenv()["CONSUMER_APP_DIR"]!!
private var FLEET_APP_DIR = System.getenv()["FLEET_APP_DIR"]!!
private var BOT_TOKEN = System.getenv()["SLACK_TOKEN"]!!
private var O_AUTH_TOKEN = System.getenv()["O_AUTH_TOKEN"]!!
private var DB_URL = System.getenv()["DB_URL"]!!
private var DB_USER = System.getenv()["DB_USER"]!!
private var DB_PASSWORD = System.getenv()["DB_PASSWORD"]!!

@Suppress("unused") // Referenced in application.conf
fun Application.module() {

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

    val database = Database(this, DB_URL, DB_USER, DB_PASSWORD)
    fetchBotData(database, BOT_TOKEN)
    val apps = mutableListOf(Constants.Common.APP_CONSUMER, Constants.Common.APP_FLEET)
    runBlocking { database.addApps(apps) }

    GlobalScope.launch {
        val branches = fetchAllBranches(GRADLE_PATH, CONSUMER_APP_DIR)
        branches?.let {
            database.addBranches(it, Constants.Common.APP_CONSUMER)
        }
    }

    routing {
        status()
        health()
        buildConsumer(GRADLE_PATH, UPLOAD_DIR_PATH, CONSUMER_APP_DIR, BOT_TOKEN)
        buildFleet(GRADLE_PATH, UPLOAD_DIR_PATH, FLEET_APP_DIR, BOT_TOKEN)
        receiveApk(UPLOAD_DIR_PATH)
        slackEvent(O_AUTH_TOKEN, database, GRADLE_PATH, CONSUMER_APP_DIR)
        subscribe()
        slackAction(O_AUTH_TOKEN, CONSUMER_APP_DIR, GRADLE_PATH)
        githubWebhook(database)
    }
}

fun fetchAllBranches(gradlePath: String, dirName: String): List<String>? {
    val executableCommand = "$gradlePath fetchRemoteBranches -P${Constants.Common.ARG_OUTPUT_SEPARATOR}=${Constants.Common.OUTPUT_SEPARATOR}"
    val response = executableCommand.execute(File(dirName))
    response?.let {
        val parsedResponse = it.split(Constants.Common.OUTPUT_SEPARATOR)
        if (parsedResponse.size >= 2) {
            return parsedResponse[1]
                .split("\n")
                .filter { item -> item.isNotEmpty() }
                .map { item -> item.substringAfter("origin/") }
        }
    }
    return null
}


@Location("/app")
data class Apk(val file: File)


