package com.ramukaka

import com.ramukaka.data.Database
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
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import io.ktor.util.error
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import network.fetchBotData
import network.health
import network.receiveApk
import network.status
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.File

fun main(args: Array<String>): Unit = EngineMain.main(args)

private var UPLOAD_DIR_PATH = "${System.getProperty("user.dir")}/temp"
private var GRADLE_PATH = "./gradlew"
private var CONSUMER_APP_DIR = System.getenv()["CONSUMER_APP_DIR"]!!
private var FLEET_APP_DIR = System.getenv()["FLEET_APP_DIR"]!!
private var BOT_TOKEN = System.getenv()["SLACK_TOKEN"]!!
private var DEFAULT_APP_URL = System.getenv()["DEFAULT_APP_URL"]!!
private var O_AUTH_TOKEN = System.getenv()["O_AUTH_TOKEN"]!!
private var DB_URL = System.getenv()["DB_URL"]!!
private var DB_USER = System.getenv()["DB_USER"]!!
private var DB_PASSWORD = System.getenv()["DB_PASSWORD"]!!

@KtorExperimentalLocationsAPI
@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val LOGGER = LoggerFactory.getLogger("com.application")
    install(StatusPages) {
        exception<Throwable> { cause ->
            LOGGER.error(cause)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

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
    runBlocking {
        fetchBotData(database, BOT_TOKEN)
    }
    val apps = mutableListOf(Constants.Common.APP_CONSUMER, Constants.Common.APP_FLEET)
    runBlocking { database.addApps(apps) }

    val gradleBotClient = GradleBotClient(GRADLE_PATH, CONSUMER_APP_DIR)

    GlobalScope.launch(Dispatchers.IO) {
        val branches = gradleBotClient.fetchAllBranches()
        branches?.let {
            database.addBranches(it, Constants.Common.APP_CONSUMER)
        }
    }

    GlobalScope.launch(Dispatchers.IO) {
        val productFlavours = gradleBotClient.fetchProductFlavours()
        productFlavours?.let {
            database.addFlavours(it, Constants.Common.APP_CONSUMER)
        }
    }

    GlobalScope.launch(Dispatchers.IO) {
        val buildVariants = gradleBotClient.fetchBuildVariants()
        buildVariants?.let {
            database.addBuildVariants(it, Constants.Common.APP_CONSUMER)
        }
    }

    val slackClient = SlackClient(O_AUTH_TOKEN, DEFAULT_APP_URL, GRADLE_PATH, UPLOAD_DIR_PATH, gradleBotClient, database)

    routing {
        status()
        health()
        buildConsumer(CONSUMER_APP_DIR, slackClient)
        buildFleet(FLEET_APP_DIR, slackClient)
        receiveApk(UPLOAD_DIR_PATH)
        slackEvent(database, slackClient)
        subscribe()
        slackAction(database, slackClient, CONSUMER_APP_DIR)
        githubWebhook(database, slackClient)
    }
}

@Location("/app")
data class Apk(val file: File)


