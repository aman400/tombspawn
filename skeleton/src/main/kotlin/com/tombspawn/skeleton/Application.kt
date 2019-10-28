package com.tombspawn.skeleton

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tombspawn.base.annotations.DoNotSerialize
import com.tombspawn.base.common.exhaustive
import com.tombspawn.base.config.JsonApplicationConfig
import com.tombspawn.base.di.LOGGER
import com.tombspawn.base.di.gsonModule
import com.tombspawn.base.di.httpClientModule
import com.tombspawn.skeleton.di.config
import com.tombspawn.skeleton.di.gradleBotClient
import com.tombspawn.skeleton.git.CredentialProvider
import com.tombspawn.skeleton.git.GitClient
import com.tombspawn.skeleton.models.App
import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.koin.core.parameter.parametersOf
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

fun main(args: Array<String>) {
    val env = applicationEngineEnvironment {
        module {
            module()
        }

        connector {
            this.host = "0.0.0.0"
            this.port = 8080
        }

        args.takeIf { it.isNotEmpty() }?.first()?.let {
            LOGGER.debug(it)
            config = JsonApplicationConfig(Gson(), it)
        }
    }
    embeddedServer(Netty, env).start(true)
}

@KtorExperimentalLocationsAPI
@Suppress("unused")
// Referenced in application.conf
fun Application.module() {

    val LOGGER = LoggerFactory.getLogger("com.tombspawn.skeleton.Application")

    val app: App by inject {
        parametersOf(this, this as CoroutineScope)
    }

    val credentialProvider: CredentialProvider by inject {
        parametersOf(this)
    }

    val gitClient: GitClient by inject {
        parametersOf(app, credentialProvider)
    }


    install(Koin) {
        modules(listOf(gsonModule, httpClientModule, config, gradleBotClient))
        logger(object : Logger() {
            override fun log(level: org.koin.core.logger.Level, msg: MESSAGE) {
                when (level) {
                    org.koin.core.logger.Level.DEBUG -> {
                        LOGGER.debug(msg)
                    }
                    org.koin.core.logger.Level.INFO -> {
                        LOGGER.info(msg)
                    }
                    org.koin.core.logger.Level.ERROR -> {
                        LOGGER.error(msg)
                    }
                }.exhaustive
            }
        })
    }

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

    runBlocking {
         gitClient.clone()
    }

    routing {

        get("/app/generate/") {
//            app.gradleExecutor?.generateApp()
        }

        get("/branches") {
            app.gradleExecutor?.fetchAllBranches()?.let {
                call.respond(it)
            } ?: call.respond("[]")
        }

        get("/flavours") {
            app.gradleExecutor?.fetchProductFlavours()?.let {
                call.respond(it)
            } ?: call.respond("[]")
        }
    }
}