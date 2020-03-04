package com.tombspawn.skeleton

import com.tombspawn.base.common.SuccessResponse
import com.tombspawn.base.config.JsonApplicationConfig
import com.tombspawn.base.di.DaggerCoreComponent
import com.tombspawn.skeleton.di.DaggerAppComponent
import com.tombspawn.skeleton.locations.References
import com.tombspawn.skeleton.models.config.ServerConf
import io.ktor.application.*
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
import io.ktor.util.error
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import javax.inject.Inject

class Skeleton(val args: Array<String>) {
    @Inject
    lateinit var applicationService: ApplicationService

    fun startServer() {
        val coreComponent = DaggerCoreComponent.create()
        val env = applicationEngineEnvironment {
            module {
                DaggerAppComponent.builder()
                    .plus(this)
                    .plus(coreComponent)
                    .build()
                    .inject(this@Skeleton)
                module(applicationService)
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

        val server = embeddedServer(GRPC, env, configure = {
            configuration = {
                addService(SkeletonGrpcService(applicationService))
            }
        })

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                LOGGER.debug("Stopping server gracefully")
                server.stop(3000, 5000)
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
fun Application.module(applicationService: ApplicationService) {
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

    environment.monitor.subscribe(ApplicationStopping) {
        LOGGER.debug("Clearing data")
        applicationService.clear()
        LOGGER.debug("Data cleared")
    }

    routing {
    }

    applicationService.init()
}