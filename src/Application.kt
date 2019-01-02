package com.ramukaka

import com.ramukaka.data.Database
import com.ramukaka.extensions.commandExecutor
import com.ramukaka.models.CommandResponse
import com.ramukaka.network.*
import com.ramukaka.utils.Constants
import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
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
import kotlinx.coroutines.*
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
private var CONSUMER_APP_URL = System.getenv()["CONSUMER_APP_URL"]!!
private var FLEET_APP_URL = System.getenv()["FLEET_APP_URL"]!!
private var O_AUTH_TOKEN = System.getenv()["O_AUTH_TOKEN"]!!
private var DB_URL = System.getenv()["DB_URL"]!!
private var DB_USER = System.getenv()["DB_USER"]!!
private var DB_PASSWORD = System.getenv()["DB_PASSWORD"]!!
private var BASE_URL = System.getenv()["BASE_URL"]!!
private var CONSUMER_APP_ID = System.getenv()["CONSUMER_APP_GITHUB_REPO_ID"]
private var FLEET_APP_ID = System.getenv()["FLEET_APP_GITHUB_REPO_ID"]

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
        filter { call -> call.request.path().startsWith("/slack/app") }
        filter { call -> call.request.path().startsWith("/github") }
        filter { call -> call.request.path().startsWith("/api/mock") }
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

    val responseListener = mutableMapOf<String, CompletableDeferred<CommandResponse>>()
    val requestExecutor = commandExecutor(responseListener)

    val gradleBotClient = GradleBotClient(GRADLE_PATH, responseListener, requestExecutor)

    launch {
        initApp(gradleBotClient, database, CONSUMER_APP_DIR, Constants.Common.APP_CONSUMER)
        initApp(gradleBotClient, database, FLEET_APP_DIR, Constants.Common.APP_FLEET)
    }

    launch {
        database.addVerbs(
            listOf(
                Constants.Common.GET,
                Constants.Common.PUT,
                Constants.Common.POST,
                Constants.Common.DELETE,
                Constants.Common.PATCH,
                Constants.Common.HEAD,
                Constants.Common.OPTIONS
            )
        )
    }

    val slackClient = SlackClient(
        O_AUTH_TOKEN, GRADLE_PATH, UPLOAD_DIR_PATH, gradleBotClient,
        database, BOT_TOKEN, requestExecutor, responseListener
    )

    routing {
        status()
        health()
        buildConsumer(CONSUMER_APP_DIR, slackClient, database, CONSUMER_APP_URL)
        buildFleet(FLEET_APP_DIR, slackClient, database, FLEET_APP_URL)
        receiveApk(UPLOAD_DIR_PATH)
        slackEvent(database, slackClient)
        subscribe()
        slackAction(database, slackClient, CONSUMER_APP_DIR, BASE_URL, FLEET_APP_DIR, CONSUMER_APP_URL, FLEET_APP_URL)
        githubWebhook(database, slackClient, CONSUMER_APP_ID!!, FLEET_APP_ID!!)
        mockApi(database)
        createApi(slackClient, database)
    }
}


suspend fun initApp(gradleBotClient: GradleBotClient, database: Database, appDir: String, appName: String)= withContext(Dispatchers.IO) {
    launch {
        val branches = gradleBotClient.fetchAllBranches(appDir)
        branches?.let {
            database.addBranches(it, appName)
        }
    }

    launch {
        val productFlavours = gradleBotClient.fetchProductFlavours(appDir)
        productFlavours?.let {
            database.addFlavours(it, appName)
        }
    }

    launch {
        val buildVariants = gradleBotClient.fetchBuildVariants(appDir)
        buildVariants?.let {
            database.addBuildVariants(it, appName)
        }
    }
}

@Location("/app")
data class Apk(val file: File)


