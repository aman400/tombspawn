package com.tombspawn.skeleton

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.tombspawn.base.common.*
import com.tombspawn.base.config.JsonApplicationConfig
import com.tombspawn.base.di.LOGGER
import com.tombspawn.base.di.gsonModule
import com.tombspawn.base.di.httpClientModule
import com.tombspawn.base.extensions.await
import com.tombspawn.base.network.MultiPartContent
import com.tombspawn.skeleton.di.config
import com.tombspawn.skeleton.di.gradleBotClient
import com.tombspawn.skeleton.locations.References
import com.tombspawn.skeleton.models.App
import com.tombspawn.skeleton.models.RefType
import com.tombspawn.skeleton.models.Reference
import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpMethod
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.koin.core.parameter.parametersOf
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.File

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
fun Application.module() {

    val LOGGER = LoggerFactory.getLogger("com.tombspawn.skeleton.Application")

    val app: App by inject {
        parametersOf(this, this as CoroutineScope)
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
        app.clone()
    }

    routing {
        get("/app/generate/") {
            val params = this.call.request.queryParameters.toMap().mapValues { values ->
                values.value.first()
            }.toMutableMap()

            val userAppPrefix = params["APP_PREFIX"]?.trim()
            val callbackUri = params["CALLBACK_URI"]?.trim()

            val apkPrefix = "${userAppPrefix?.let {
                "$it-"
            } ?: ""}${System.currentTimeMillis()}"

            params.remove("APP_PREFIX")
            val uploadDirPath = "temp"

            val response = app.gradleExecutor?.generateApp(params, uploadDirPath, apkPrefix)
            val httpClient: HttpClient = this@module.get(null) {
                parametersOf(null, null, null)
            }
            when(response) {
                is Success -> {
                    callbackUri?.let { url ->
                        val tempDirectory = File(uploadDirPath)
                        if (tempDirectory.exists()) {
                            tempDirectory.listFiles { _, name ->
                                name.contains(apkPrefix, true)
                            }?.firstOrNull()?.let { file ->
                                if (file.exists()) {
                                    val responseData = httpClient.call(url) {
                                        method = HttpMethod.Post
                                        body = MultiPartContent.build {
                                            add("prefix", apkPrefix)
                                            add("file", file.readBytes(), filename = file.name)
                                        }
                                    }.await<JsonObject>()

                                    when(responseData) {
                                        is CallSuccess -> {
                                            LOGGER.debug("Uploaded successfully")
                                        }
                                        is CallFailure -> {
                                            responseData.throwable?.let {
                                                LOGGER.error(it)
                                            }
                                        }
                                        is CallError -> {
                                            responseData.throwable?.let {
                                                LOGGER.error(it)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } ?: run {
                        LOGGER.error("Callback uri is null")
                    }
                }
                is Failure -> {
                    response.throwable?.printStackTrace()
                }
                null -> {}
            }
        }

        get("/build-variants") {
            app.gradleExecutor?.fetchBuildVariants()?.let {
                call.respond(it)
            } ?: call.respond("[]")
        }

        this@routing.get<References> { reference ->
            val branchLimit = reference.branchLimit
            val tagLimit = reference.tagLimit
            app.fetchRemotesAsync()?.await()
            call.respond(mutableListOf<Reference>().apply {
                app.getBranchesAsync()?.await()?.map { branch ->
                    Reference(branch, RefType.BRANCH)
                }?.let {
                    if(branchLimit >= 0) {
                        addAll(it.take(branchLimit))
                    } else {
                        addAll(it)
                    }
                }
                app.getTagsAsync()?.await()?.map { tag ->
                    Reference(tag, RefType.TAG)
                }?.let {
                    if(tagLimit >= 0) {
                        addAll(it.take(tagLimit))
                    } else {
                        addAll(it)
                    }
                }
            })
        }

        get("/flavours") {
            app.gradleExecutor?.fetchProductFlavours()?.let {
                call.respond(it)
            } ?: call.respond("[]")
        }
    }
}