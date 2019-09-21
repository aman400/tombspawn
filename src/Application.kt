package com.ramukaka

import com.google.gson.Gson
import com.ramukaka.auth.*
import com.ramukaka.auth.sessions.SlackSession
import com.ramukaka.data.Database
import com.ramukaka.data.Redis
import com.ramukaka.data.StringMap
import com.ramukaka.di.*
import com.ramukaka.extensions.commandExecutor
import com.ramukaka.extensions.isDebug
import com.ramukaka.models.CommandResponse
import com.ramukaka.models.config.App
import com.ramukaka.models.config.Common
import com.ramukaka.models.config.JWT
import com.ramukaka.models.config.Slack
import com.ramukaka.network.GradleBotClient
import com.ramukaka.network.exhaustive
import com.ramukaka.network.githubWebhook
import com.ramukaka.slackbot.*
import com.ramukaka.utils.Constants
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.client.HttpClient
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import io.ktor.sessions.*
import io.ktor.util.error
import io.ktor.util.hex
import kotlinx.coroutines.*
import network.fetchBotData
import network.health
import network.receiveApk
import network.status
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.StringQualifier
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.time.Duration

fun main(args: Array<String>): Unit = EngineMain.main(args)

@KtorExperimentalLocationsAPI
@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val LOGGER = LoggerFactory.getLogger("com.application")

    val jwt: JWT by inject {
        parametersOf(this)
    }

    val apps: List<App> by inject {
        parametersOf(this)
    }

    val slack: Slack by inject {
        parametersOf(this)
    }

    val common: Common by inject {
        parametersOf(this)
    }

    val jwtConfig: JWTConfig by inject {
        parametersOf(jwt.secret, jwt.domain, jwt.audience)
    }

    val uploadDirPath: String by inject(StringQualifier(Constants.EnvironmentVariables.ENV_UPLOAD_DIR_PATH))
    val gson: Gson by inject()

    install(Koin) {
        modules(listOf(dbModule, httpClientModule, gradleBotClient,
            slackModule, gsonModule, redis, authentication, config))
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

    install(CORS) {
        method(HttpMethod.Get)
        method(HttpMethod.Put)
        method(HttpMethod.Post)
        method(HttpMethod.Delete)
        method(HttpMethod.Options)
        header(HttpHeaders.XForwardedProto)
        header(HttpHeaders.AccessControlAllowCredentials)
        header(HttpHeaders.AccessControlAllowHeaders)
        header(HttpHeaders.AccessControlAllowMethods)
        header(HttpHeaders.AccessControlAllowOrigin)
//        anyHost()

//        host("192.168.0.101:3000", listOf("http", "https"))
//        host("10.1.1.179:3000", listOf("http", "https"))
//        host("localhost:3000", listOf("http", "https"))
//        host("127.0.0.1:3000", listOf("http", "https"))

        allowCredentials = true
        allowSameOrigin = true
        maxAge = Duration.ofDays(1)
    }

    val database: Database by inject {
        parametersOf(this, isDebug)
    }

    install(Authentication) {
        jwt(name = "slack-auth") {
            verifier(jwtConfig.verifier)
            realm = jwt.realm
            validate { credential ->
                credential.payload.getClaim("id").asString()?.let { database.findUser(it) }
            }
            skipWhen {
                call ->
                    try {
                        jwtConfig.verifier.verify(call.sessions.get<SlackSession>()?.token) != null
                    } catch (exception: Exception) {
                        false
                    }
            }
        }
    }

    install(StatusPages) {
        exception<Throwable> { cause ->
            println(cause.stackTrace)
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


    val client: HttpClient by inject()

    val authMap: StringMap by inject {
        parametersOf(Redis.AUTH_MAP)
    }

    val sessionMap: StringMap by inject {
        parametersOf(Redis.SESSION_MAP)
    }

    install(Sessions) {
        val secretHashKey = hex("6819b57a326945c1968f45236589")

        cookie<SlackSession>(Constants.Slack.SESSION, RedisSessionStorage(sessionMap, "Session_", 3600)) {
            cookie.extensions
            cookie.path = "/"
            if(!isDebug) {
                cookie.secure = true
            }
            transform(SessionTransportTransformerMessageAuthentication(secretHashKey, "HmacSHA256"))
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

    runBlocking {
        fetchBotData(client, database, slack.botToken)
    }
    runBlocking { database.addApps(apps) }

    val responseListener = mutableMapOf<String, CompletableDeferred<CommandResponse>>()
    val requestExecutor = commandExecutor(responseListener)

    val gradleBotClient: GradleBotClient by inject { parametersOf(this, responseListener, requestExecutor) }

    launch(Dispatchers.IO) {
        apps.forEach {
            initApp(gradleBotClient, database, it.dir, it.id)
        }
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

    val slackClient: SlackClient by inject { parametersOf(this, responseListener, requestExecutor) }
    launch(Dispatchers.IO) {
        val users = slackClient.getSlackUsers(slack.botToken, slackClient, null)
        val ims = slackClient.getSlackBotImIds(slack.botToken, slackClient, null)
        users.forEach { user ->
            val im = ims.firstOrNull { im ->
                im.user == user.id
            }
            im?.let {
                if (im.isUserDeleted == false && user.bot == false && user.id != Constants.Slack.DEFAULT_BOT_ID) {
                    database.addUser(
                        user.id!!,
                        user.profile?.name,
                        user.profile?.email,
                        Constants.Database.USER_TYPE_USER,
                        im.id
                    )
                }
            }
        }
    }

    routing {
        status()
        health()
        buildApp(apps, slackClient, database)
        slackEvent(database, slackClient)
        subscribe(slackClient, database, apps)
        slackAction(database, slackClient, common.baseUrl, apps, gson)
        githubWebhook(apps, database, slackClient)
        mockApi(database)
        createApi(slackClient, database)
//        standup(slackClient)
//        auth(slackClient, jwtConfig, database)
//        users(database)
    }
}

suspend fun initApp(gradleBotClient: GradleBotClient, database: Database, appDir: String, appName: String) =
    coroutineScope {
        val branchJob = async {
            val branches = gradleBotClient.fetchAllBranches(appDir)
            branches?.let {
                database.addRefs(it, appName)
            }
        }

        val flavourJob = async {
            val productFlavours = gradleBotClient.fetchProductFlavours(appDir)
            productFlavours?.let {
                database.addFlavours(it, appName)
            }
        }

        val variantJob = async {
            val buildVariants = gradleBotClient.fetchBuildVariants(appDir)
            buildVariants?.let {
                database.addBuildVariants(it, appName)
            }
        }

        branchJob.start()
        flavourJob.start()
        variantJob.start()
    }