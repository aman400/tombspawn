package com.tombspawn

import com.tombspawn.base.config.JsonApplicationConfig
import com.tombspawn.base.di.DaggerCoreComponent
import com.tombspawn.di.AppComponent
import com.tombspawn.di.DaggerAppComponent
import com.tombspawn.models.config.ServerConf
import com.tombspawn.network.githubWebhook
import com.tombspawn.network.health
import com.tombspawn.network.status
import com.tombspawn.slackbot.*
import com.tombspawn.utils.Constants
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.error
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.File
import java.util.concurrent.TimeUnit

class Grave(val args: Array<String>) {
    private val LOGGER = LoggerFactory.getLogger("com.tombspawn.Grave")

    @ExperimentalStdlibApi
    fun startServer() {
        val coreComponent = DaggerCoreComponent.create()
        val env = applicationEngineEnvironment {
            module {
                val appComponent = DaggerAppComponent.builder()
                    .plus(this)
                    .plus(coreComponent)
                    .build()
                module(appComponent)
            }

            var host = Constants.Common.DEFAULT_HOST
            var port = Constants.Common.DEFAULT_PORT
            args.firstOrNull()?.let { fileName ->
                File(fileName).bufferedReader().use {
                    val text = it.readText()
                    config = JsonApplicationConfig(coreComponent.gson(), text).also { jsonConf ->
                        jsonConf.propertyOrNull("server")?.getAs(ServerConf::class.java)?.let { conf ->
                            conf.host?.let {
                                host = it
                            }
                            conf.port?.let {
                                port = it
                            }
                        }
                    }
                }
            }

            connector {
                this.host = host
                this.port = port
            }
        }
        val server = embeddedServer(Netty, env)

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                LOGGER.debug("Stopping server gracefully")
                server.stop(3000, 5000)
            }
        })
        server.start(true)
    }

    companion object {
        @ExperimentalStdlibApi
        @JvmStatic
        fun main(args: Array<String>) {
            DaggerCoreComponent.create()
            Grave(args).startServer()
        }
    }
}

@ExperimentalStdlibApi
@KtorExperimentalLocationsAPI
@Suppress("unused") // Referenced in application.conf
fun Application.module(appComponent: AppComponent) {
    val LOGGER = LoggerFactory.getLogger("com.tombspawn.grave.Application")

    launch(Dispatchers.IO) {
        appComponent.applicationService().init()
    }

    environment.monitor.subscribe(ApplicationStopping) {
        LOGGER.debug("Clearing data")
        appComponent.applicationService().clear()
        LOGGER.debug("Data cleared")
    }

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
        level = Level.TRACE
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
            LOGGER.debug("Parameters: ")
            call.parameters.forEach { key, valuesList ->
                LOGGER.debug("$key: ${valuesList.joinToString(",")}")
            }
        }

        if (!call.request.headers.isEmpty()) {
            LOGGER.debug("Headers: ")
            call.request.headers.forEach { key, valuesList ->
                LOGGER.debug("$key: ${valuesList.joinToString(",")}")
            }
        }

        if (!call.request.queryParameters.isEmpty()) {
            LOGGER.debug("Query Params: ")
            call.request.queryParameters.forEach { key, valuesList ->
                LOGGER.debug("$key: ${valuesList.joinToString(",")}")
            }
        }
    }

    routing {
        status()
        health()
        buildApp(appComponent.applicationService())
        apkCallback(appComponent.applicationService())
        slackEvent(appComponent.applicationService())
        subscribe(appComponent.applicationService())
        slackAction(appComponent.applicationService())
        githubWebhook(appComponent.applicationService())
        mockApi(appComponent.applicationService())
        createApi(appComponent.applicationService())
    }
}