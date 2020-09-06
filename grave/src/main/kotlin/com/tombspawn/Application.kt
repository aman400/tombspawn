package com.tombspawn

import com.tombspawn.auth.makeJwtVerifier
import com.tombspawn.base.config.JsonApplicationConfig
import com.tombspawn.base.di.DaggerCoreComponent
import com.tombspawn.di.DaggerAppComponent
import com.tombspawn.models.config.JWTConfig
import com.tombspawn.models.config.ServerConf
import com.tombspawn.network.githubWebhook
import com.tombspawn.network.health
import com.tombspawn.network.status
import com.tombspawn.session.GsonSessionSerializer
import com.tombspawn.session.RedisSessionStorage
import com.tombspawn.session.models.LoginSession
import com.tombspawn.slackbot.*
import com.tombspawn.utils.Constants
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

class Grave(val args: Array<String>) {
    private val LOGGER = LoggerFactory.getLogger("com.tombspawn.Grave")

    @Inject
    lateinit var applicationService: ApplicationService

    @Inject
    lateinit var jwtConfig: JWTConfig

    @Inject
    lateinit var sessionStorage: RedisSessionStorage

    @ExperimentalStdlibApi
    fun startServer() {
        val coreComponent = DaggerCoreComponent.create()
        val env = applicationEngineEnvironment {
            module {
                DaggerAppComponent
                    .factory()
                    .create(this, coreComponent)
                    .inject(this@Grave)
                module(applicationService, jwtConfig, sessionStorage)
            }

            var host = Constants.Common.DEFAULT_HOST
            var port = Constants.Common.DEFAULT_PORT
            args.firstOrNull()?.let { fileName ->
                File(fileName).bufferedReader().use {
                    val text = it.readText()
                    config = JsonApplicationConfig(coreComponent.gson(), text).also { jsonConf ->
                        jsonConf.propertyOrNull("ktor.deployment")?.getAs(ServerConf::class.java)?.let { conf ->
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

@OptIn(ExperimentalTime::class, KtorExperimentalAPI::class, KtorExperimentalLocationsAPI::class)
@ExperimentalStdlibApi
@Suppress("unused") // Referenced in application.conf
fun Application.module(
    applicationService: ApplicationService,
    jwtConfig: JWTConfig,
    sessionStorage: RedisSessionStorage
) {
    val LOGGER = LoggerFactory.getLogger("com.tombspawn.grave.Application")

    launch(Dispatchers.IO) {
        applicationService.init()
    }

    environment.monitor.subscribe(ApplicationStopping) {
        LOGGER.debug("Clearing data")
        applicationService.clear()
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

    install(Sessions) {
        cookie<LoginSession>("LOGIN_COOKIE", sessionStorage) {
            cookie.path = "/"
            cookie.httpOnly = false
            serializer = GsonSessionSerializer(LoginSession::class.java)
            val secretSignKey = hex("62533132736d4f324c6d6e78")
            transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
        }
    }

    install(CORS) {
        allowCredentials = true
        allowSameOrigin = true
        allowNonSimpleContentTypes = true
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        header(HttpHeaders.XForwardedProto)
        header(HttpHeaders.AccessControlAllowHeaders)
        header(HttpHeaders.ContentType)
        header(HttpHeaders.SetCookie)
        header(HttpHeaders.AccessControlAllowOrigin)
        anyHost()
        host("localhost:8081", listOf("http", "https"))
        host("127.0.0.1:8081", listOf("http", "https"))
        host("0.0.0.0:8081", listOf("http", "https"))
        maxAgeDuration = 1.toDuration(DurationUnit.DAYS)
    }

    install(Authentication) {
        jwt(Constants.Common.ADMIN_AUTH) {
            realm = jwtConfig.realm
            verifier(makeJwtVerifier(jwtConfig))
            challenge { scheme, realm ->
                call.respond(
                    UnauthorizedResponse(
                        HttpAuthHeader.Parameterized(
                            scheme,
                            mapOf(HttpAuthHeader.Parameters.Realm to realm)
                        )
                    )
                )
            }
            validate { credential ->
                if (credential.payload.audience.contains(jwtConfig.audience)) JWTPrincipal(credential.payload) else null
            }
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
        buildApp(applicationService)
        apkCallback(applicationService)
        slackEvent(applicationService)
        subscribe(applicationService)
        slackAction(applicationService)
        githubWebhook(applicationService)
    }
}