package com.tombspawn.skeleton

import com.tombspawn.base.common.CommonConstants
import com.tombspawn.base.config.JsonApplicationConfig
import com.tombspawn.base.di.DaggerCoreComponent
import com.tombspawn.skeleton.di.AppComponent
import com.tombspawn.skeleton.di.DaggerAppComponent
import com.tombspawn.skeleton.locations.References
import com.tombspawn.skeleton.models.config.CommonConfig
import com.tombspawn.skeleton.models.config.ServerConf
import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.locations.get
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.error
import io.ktor.util.toMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.util.concurrent.TimeUnit

class Skeleton(val args: Array<String>) {
    fun startServer() {
        val coreComponent = DaggerCoreComponent.create()
        val env = applicationEngineEnvironment {
            module {
                val appComponent: AppComponent = DaggerAppComponent.builder()
                    .plus(this)
                    .plus(coreComponent)
                    .build()
                module(appComponent)
            }

            var host = "0.0.0.0"
            var port = 8080
            args.takeIf { it.isNotEmpty() }?.first()?.let {
                config = JsonApplicationConfig(coreComponent.gson(), it).also { jsonConf ->
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

            connector {
                this.host = host
                this.port = port
            }
        }

        val server = embeddedServer(Netty, env)

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                LOGGER.debug("Stopping server gracefully")
                server.stop(3, 5, TimeUnit.SECONDS)
            }
        })
        server.start(true)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger("com.tombspawn.skeleton.Skeleton")
        @JvmStatic
        fun main(args: Array<String>) {
            Skeleton(args).startServer()
        }
    }
}

@KtorExperimentalLocationsAPI
@Suppress("unused")
fun Application.module(appComponent: AppComponent) {

    val LOGGER = LoggerFactory.getLogger("com.tombspawn.skeleton.Application")

    install(Locations) { }

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

    install(StatusPages) {
        exception<Throwable> { cause ->
            LOGGER.error(cause)
            call.respond(HttpStatusCode.InternalServerError)
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
            LOGGER.debug("Headers:")
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

    val applicationService = appComponent.applicationService()

    runBlocking {
        applicationService.init()
    }

    routing {
        get("/app/generate") {
            val params = this.call.request.queryParameters.toMap().mapValues { values ->
                values.value.first()
            }.toMutableMap()

            val userAppPrefix = params[CommonConstants.APP_PREFIX]?.trim() ?: ""
            val successCallbackUri = params[CommonConstants.SUCCESS_CALLBACK_URI]?.trim()
            val failureCallbackUri = params[CommonConstants.FAILURE_CALLBACK_URI]?.trim()

            params.remove(CommonConstants.APP_PREFIX)
            params.remove(CommonConstants.SUCCESS_CALLBACK_URI)
            params.remove(CommonConstants.FAILURE_CALLBACK_URI)

            launch(Dispatchers.IO) {
                applicationService.generateApp(params, successCallbackUri, failureCallbackUri, userAppPrefix)
            }
            call.respond("{\"message\": \"ok\"}")
        }

        get("/build-variants") {
            applicationService.fetchBuildVariants()?.let {
                call.respond(it)
            } ?: call.respond("[]")
        }

        this@routing.get<References> { reference ->
            val branchLimit = reference.branchLimit
            val tagLimit = reference.tagLimit
            applicationService.fetchRemoteBranches()
            call.respond(applicationService.getReferences(branchLimit, tagLimit))
        }

        get("/flavours") {
            applicationService.fetchProductFlavours()?.let {
                call.respond(it)
            } ?: call.respond("[]")
        }
    }
}