package com.tombspawn

import com.google.gson.Gson
import com.tombspawn.base.common.CallError
import com.tombspawn.base.common.CallFailure
import com.tombspawn.base.common.CallSuccess
import com.tombspawn.base.common.exhaustive
import com.tombspawn.base.config.JsonApplicationConfig
import com.tombspawn.base.di.gsonModule
import com.tombspawn.base.di.httpClientModule
import com.tombspawn.base.extensions.await
import com.tombspawn.base.extensions.isDebug
import com.tombspawn.data.Database
import com.tombspawn.data.StringMap
import com.tombspawn.di.*
import com.tombspawn.docker.createContainers
import com.tombspawn.git.CredentialProvider
import com.tombspawn.models.Reference
import com.tombspawn.models.config.*
import com.tombspawn.network.docker.DockerApiClient
import com.tombspawn.network.fetchBotData
import com.tombspawn.network.githubWebhook
import com.tombspawn.network.health
import com.tombspawn.network.status
import com.tombspawn.slackbot.*
import com.tombspawn.utils.Constants
import io.ktor.application.*
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
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
import kotlinx.coroutines.*
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.StringQualifier
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.File
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    val env = applicationEngineEnvironment {
        module {
            module()
        }

        var host = "0.0.0.0"
        var port = 80
        args.firstOrNull()?.let { fileName ->
            File(fileName).bufferedReader().use {
                val text = it.readText()
                config = JsonApplicationConfig(Gson(), text).also {
                    it.propertyOrNull("server")?.getAs(ServerConf::class.java)?.let {
                        it.host?.let {
                            host = it
                        }
                        it.port?.let {
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
    val server = embeddedServer(Netty, env).start(true)

    Runtime.getRuntime().addShutdownHook(Thread {
        server.stop(1, 7, TimeUnit.SECONDS)
    })
    Thread.currentThread().join()
}

@KtorExperimentalLocationsAPI
@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val LOGGER = LoggerFactory.getLogger("com.application")

    val apps: List<App> by inject {
        parametersOf(this, this as CoroutineScope)
    }

    val slack: Slack by inject {
        parametersOf(this)
    }

    val common: Common by inject {
        parametersOf(this)
    }

    val credentialProvider: CredentialProvider by inject {
        parametersOf(this)
    }

    val dockerApiClient by inject<DockerApiClient>()

    val uploadDirPath: String by inject(StringQualifier(Constants.EnvironmentVariables.ENV_UPLOAD_DIR_PATH))
    val gson: Gson by inject()

    install(Koin) {
        modules(
            listOf(
                dbModule, httpClientModule, gradleBotClient, redisClient,
                slackModule, gsonModule, config, docker
            )
        )
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

    val database: Database by inject {
        parametersOf(this, isDebug)
    }

    environment.monitor.subscribe(ApplicationStopping) {
        LOGGER.debug("Clearing data")
        database.clear()
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

    val slackHttpClient: HttpClient by inject {
        parametersOf("slack.com", URLProtocol.HTTPS, null)
    }

    val sessionMap: StringMap by inject {
        parametersOf(this)
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
        fetchBotData(slackHttpClient, database, slack.botToken)
    }
    runBlocking { database.addApps(apps) }

    launch(Dispatchers.IO) {
        createContainers(dockerApiClient, apps, common, credentialProvider, gson)
    }

//    launch(Dispatchers.IO) {
//        apps.forEach {
//            initApp(it, database, it.dir ?: "/", it.id)
//        }
//    }

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

    val slackClient: SlackClient by inject { parametersOf(this) }
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
        get("/branches") {
            apps.first().networkClient?.let { client ->
                val call = client.call  {
                    method = HttpMethod.Get
                    url {
                        encodedPath = "/branches"
//                        parameter("token", botToken)
                    }
                }

                when (val response = call.await<List<Reference>>()) {
                    is CallSuccess -> {
                        response.data?.let { references ->
                            this.call.respond(references)
                        }
                    }
                    is CallFailure -> {
                        println(response.errorBody)
                    }
                    is CallError -> {
                        response.throwable?.printStackTrace()
                    }
                }.exhaustive
            }
        }

        get("/flavours") {
            println("requesting ")
            println("requesting ${apps.first().appUrl}")
//            launch(Dispatchers.IO) {
                apps.first().networkClient?.let { client ->
                    println("requesting ${apps.first().appUrl}")
                    val call = client.call {
                        method = HttpMethod.Get
                        url {
                            encodedPath = "/flavours"
//                        parameter("token", botToken)
                        }
                    }

                    when (val response = call.await<List<String>>()) {
                        is CallSuccess -> {
                            response.data?.let { references ->
                                println(references.toString())
                            }
                        }
                        is CallFailure -> {
                            println(response.errorBody)
                        }
                        is CallError -> {
                            response.throwable?.printStackTrace()
                        }
                    }.exhaustive
                }
//            }
            call.respond("""{"flavour": "free"}""")
        }
    }
}

suspend fun initApp(app: App, database: Database, appDir: String, appName: String) =
    coroutineScope {
        val branchJob = async {
            val branches = app.gradleExecutor?.fetchAllBranches()
            branches?.let {
                database.addRefs(it, appName)
            }
        }

        val flavourJob = async {
            val productFlavours = app.gradleExecutor?.fetchProductFlavours()
            productFlavours?.let {
                database.addFlavours(it, appName)
            }
        }

        val variantJob = async {
            val buildVariants = app.gradleExecutor?.fetchBuildVariants()
            buildVariants?.let {
                database.addBuildVariants(it, appName)
            }
        }

        branchJob.start()
        flavourJob.start()
        variantJob.start()
    }