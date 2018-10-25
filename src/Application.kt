package com.ramukaka

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ramukaka.extensions.copyToSuspend
import com.ramukaka.extensions.execute
import com.ramukaka.extensions.random
import com.ramukaka.extensions.toMap
import com.ramukaka.models.ErrorResponse
import com.ramukaka.models.database.User
import com.ramukaka.models.database.Users
import com.ramukaka.models.locations.GithubApi
import com.ramukaka.models.locations.Slack
import com.ramukaka.models.slack.Attachment
import com.ramukaka.models.slack.SlackEvent
import com.ramukaka.network.RamukakaApi
import com.ramukaka.network.ServiceGenerator
import com.ramukaka.network.SlackApi
import com.ramukaka.network.interceptors.LoggingInterceptor
import com.ramukaka.utils.Constants
import com.sun.org.apache.xpath.internal.operations.Bool
import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.Parameters
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.locations.Location
import io.ktor.locations.Locations
import io.ktor.locations.post
import io.ktor.request.path
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.coroutines.launch
import models.Payload
import models.slack.Action
import models.slack.Option
import models.slack.UserProfile
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.event.Level
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.io.File
import java.util.logging.Logger

fun main(args: Array<String>): Unit = io.ktor.server.netty.DevelopmentEngine.main(args)
private val LOGGER = Logger.getLogger("MainLogger")

private var UPLOAD_DIR_PATH = "${System.getProperty("user.dir")}/temp"
private var GRADLE_PATH = System.getenv()["GRADLE_PATH"]
private var CONSUMER_APP_DIR = System.getenv()["CONSUMER_APP_DIR"]
private var FLEET_APP_DIR = System.getenv()["FLEET_APP_DIR"]
private var TOKEN = System.getenv()["SLACK_TOKEN"]
private var O_AUTH_TOKEN = System.getenv()["O_AUTH_TOKEN"]
private const val OUTPUT_SEPARATOR = "##***##"
private const val ARA_OUTPUT_SEPARATOR = "OUTPUT_SEPARATOR"


private val randomWaitingMessages = listOf(
    "Utha le re Baghwan..",
    "Jai Maharashtra!!",
    "Try Holding your Breath!!",
    "Hold your horses!!",
    "Checking Anti-Camp Radius",
    "Creating Randomly Generated Feature",
    "Doing Something You Don't Wanna Know About",
    "Doing The Impossible",
    "Don't Panic",
    "Ensuring Everything Works Perfektly",
    "Generating Plans for Faster-Than-Light Travel",
    "Hitting Your Keyboard Won't Make This Faster",
    "In The Grey, No One Can Hear You Scream",
    "Loading, Don't Wait If You Don't Want To",
    "Preparing to Spin You Around Rapidly"
)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    if (GRADLE_PATH == null || TOKEN == null) {
        throw Exception("Gradle variables GRADLE_PATH or SLACK_TOKEN not set")
    }

    Database.connect(
        url = "jdbc:mysql://localhost:3306/ramukaka?nullNamePatternMatchesAll=true",
        driver = "com.mysql.cj.jdbc.Driver",
        password = "kwerty123",
        user = "root"
    )


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

    transaction {
        SchemaUtils.create(Users)
    }

    val client = HttpClient(OkHttp) {
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
            println("Query Paras: ")
            call.request.queryParameters.forEach { key, valuesList ->
                println("$key: ${valuesList.joinToString(",")}")
            }
        }

    }

    routing {
        get("/") {
            call.respond(mapOf("status" to "OK"))
        }
        get("/health") {
            call.respond(mapOf("status" to "OK"))
        }
        post<Slack.Consumer> {
            val params = call.receiveParameters()

            val channelId = params["channel_id"]
            val text = params["text"]
            val responseUrl = params["response_url"]
            val APKPrefix = System.currentTimeMillis()

            text?.trim()?.toMap()?.let { buildData ->
                var executableCommand =
                    "$GRADLE_PATH assembleWithArgs -PFILE_PATH=$UPLOAD_DIR_PATH -PAPP_PREFIX=$APKPrefix"

                buildData.forEach { key, value ->
                    executableCommand += " -P$key=$value"
                }

                launch {
                    println(executableCommand)
                    val commandResponse = executableCommand.execute(File(CONSUMER_APP_DIR))

                    val tempDirectory = File(UPLOAD_DIR_PATH)
                    if (tempDirectory.exists()) {
                        val firstFile = tempDirectory.listFiles { dir, name ->
                            name.contains("$APKPrefix", true)
                        }.firstOrNull()
                        firstFile?.let { file ->
                            if (file.exists()) {
                                uploadFile(file, channelId!!)
                            } else sendError(commandResponse, responseUrl!!)
                        } ?: sendError(commandResponse, responseUrl!!)
                    } else {
                        sendError(commandResponse, responseUrl!!)
                    }
                }
                call.respond(randomWaitingMessages.random()!!)
            }
                ?: call.respond("Invalid command. Usage: '/build BRANCH=<git-branch-name>(optional)  BUILD_TYPE=<release/debug>(optional)  FLAVOUR=<flavour>(optional)'.")
        }

        post<Slack.Fleet> {
            val params = call.receiveParameters()

            val channelId = params["channel_id"]
            val text = params["text"]
            val APKPrefix = System.currentTimeMillis()
            val responseUrl = params["response_url"]

            text?.trim()?.toMap()?.let { buildData ->
                var executableCommand =
                    "$GRADLE_PATH assembleWithArgs -PFILE_PATH=$UPLOAD_DIR_PATH -PAPP_PREFIX=$APKPrefix"

                buildData.forEach { key, value ->
                    executableCommand += " -P$key=$value"
                }

                launch {
                    println(executableCommand)
                    val commandResponse = executableCommand.execute(File(FLEET_APP_DIR))

                    val tempDirectory = File(UPLOAD_DIR_PATH)
                    if (tempDirectory.exists()) {
                        val firstFile = tempDirectory.listFiles { dir, name ->
                            name.contains("$APKPrefix", true)
                        }.firstOrNull()
                        firstFile?.let { file ->
                            if (file.exists()) {
                                uploadFile(file, channelId!!)
                            } else sendError(commandResponse, responseUrl!!)
                        } ?: sendError(commandResponse, responseUrl!!)
                    } else {
                        sendError(commandResponse, responseUrl!!)
                    }
                }
                call.respond(randomWaitingMessages.random()!!)
            }
                ?: call.respond("Invalid command. Usage: '/build BRANCH=<git-branch-name>(optional)  BUILD_TYPE=<release/debug>(optional)  FLAVOUR=<flavour>(optional)'.")
        }

        post<Apk> {
            val multipart = call.receiveMultipart()
            var description: String
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        if (part.name == "description") {
                            description = part.value
                            println(description)
                        }
                    }
                    is PartData.FileItem -> {
                        val ext = File(part.originalFileName).extension
                        val name = File(part.originalFileName).name
                        val file = File(UPLOAD_DIR_PATH, "upload-app.$ext")
                        part.streamProvider().use { input ->
                            file.outputStream().buffered().use { output ->
                                input.copyToSuspend(output)
                                call.respond(mapOf("message" to "Upload Complete"))
                            }
                        }
                    }
                }

                part.dispose()
            }
        }

        post<Slack.Event> {
            val slackEvent = call.receive<SlackEvent>()
            println(slackEvent.toString())
            when (slackEvent.type) {
                Constants.EVENT_TYPE_VERIFICATION -> call.respond(slackEvent)
                Constants.EVENT_TYPE_RATE_LIMIT -> {
                    call.respond("")
                    println("Api rate limit")
                }
                Constants.EVENT_TYPE_CALLBACK -> {
                    call.respond("")
                    slackEvent.event?.let { event ->
                        fetchUser(event.user!!)
                        when (event.type) {
                            Constants.EVENT_TYPE_APP_MENTION -> {

                            }
                            Constants.EVENT_TYPE_MESSAGE -> {
                            }
                            else -> {

                            }
                        }
                    }

                }
                "interactive_message" -> {

                }
                else -> {
                    call.respond("")
                }
            }
        }

        post<Slack.Subscribe> {
            call.respond("")
        }

        post<Slack.Action> {
            val params = call.receive<Parameters>()
            val payload = params["payload"]
            val slackEvent = Gson().fromJson<SlackEvent>(payload, SlackEvent::class.java)
            println(slackEvent.toString())

            call.respond("")
            if (slackEvent.callbackId.equals("subscribe_generate_apk")) {

            } else {
                launch {

                    val branches = fetchAllBranches(CONSUMER_APP_DIR!!)
                    val branchList = mutableListOf<Option>()
                    branches?.forEach { branchName ->
                        branchList.add(Option(branchName, branchName))
                    }
                    val attachments = mutableListOf(
                        Attachment(
                            "subscribe_generate_apk", "Subscribe to github branch for code changes.",
                            "Select the branch to subscribe the changes for APK Generation.", 1, "#0000FF",
                            mutableListOf(
                                Action(
                                    confirm = null,
                                    name = "choose_branch",
                                    text = "Choose the branch to subscribe for changes",
                                    type = "select",
                                    options = branchList
                                )
                            )
                        )
                    )

                    val gson = Gson()

                    val body = mutableMapOf<String, String?>()
                    body["attachments"] = gson.toJson(attachments)
                    body["text"] = "Generate an APK"
                    body["channel"] = slackEvent.channel?.id
                    body["token"] = O_AUTH_TOKEN
                    val api = ServiceGenerator.createService(RamukakaApi::class.java, SlackApi.BASE_URL, true)
                    val headers = mutableMapOf("Content-type" to "application/x-www-form-urlencoded")
                    headers[""]
                    api.postAction(headers, body).enqueue(object : Callback<JsonObject> {
                        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                            println("post failure")
                        }

                        override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                            println("post success")
                        }

                    })
                }
            }
        }

        post<GithubApi.Webhook> {
            val payload = call.receive<Payload>()
            payload.ref?.let { ref ->
                if (ref == "refs/heads/development") {
                    call.respond("OK")
                } else {
                    call.respond("Not development branch")
                }
            } ?: call.respond("Not development branch")
        }
    }
}

fun fetchUser(userId: String) {
    run {
        if(!userExists(userId)) {
            val api = ServiceGenerator.createService(SlackApi::class.java, SlackApi.BASE_URL,
                true, callAdapterFactory = RxJava2CallAdapterFactory.create())
            val queryParams = mutableMapOf<String, String>()
            queryParams[SlackApi.PARAM_TOKEN] = O_AUTH_TOKEN!!
            queryParams[SlackApi.PARAM_USER_ID] = userId
            api.getProfile(queryParams).subscribe({ response ->
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        if (body.success) {
                            addUserToDB(body.user, userId)
                        }
                    }
                }
            }, { throwable ->
                LOGGER.log(java.util.logging.Level.SEVERE, throwable.message, throwable!!)
            })
        }
    }
}

fun userExists(userId: String): Boolean {
    return transaction {
        addLogger(StdOutSqlLogger)
        val user = User.find { Users.slackId eq userId }
        !user.empty()
    }
}

fun addUserToDB(user: UserProfile, userId: String) {
    transaction {
        addLogger(StdOutSqlLogger)
        User.new {
            name = user.name!!
            email = user.email!!
            slackId = userId
        }
    }
}

private fun fetchAllBranches(dirName: String): List<String>? {
    val executableCommand = "$GRADLE_PATH fetchRemoteBranches -P$ARA_OUTPUT_SEPARATOR=$OUTPUT_SEPARATOR"
    val response = executableCommand.execute(File(dirName))
    response?.let {
        val parsedResponse = it.split(OUTPUT_SEPARATOR)
        if (parsedResponse.size >= 2) {
            return parsedResponse[1].split("\n")
        }
    }
    return null
}

private fun sendError(commandResponse: String?, responseUrl: String) {
    val errorResponse = if (!commandResponse.isNullOrEmpty()) {
        ErrorResponse(response = commandResponse)
    } else {
        ErrorResponse(response = "Something went wrong. Unable to generate APK.")
    }

    val api = ServiceGenerator.createService(RamukakaApi::class.java, SlackApi.BASE_URL, true)
    val headers = mutableMapOf("Content-type" to "application/json")
    val call = api.sendError(headers, responseUrl, errorResponse)
    call.enqueue(object : Callback<String> {
        override fun onFailure(call: Call<String>, throwable: Throwable) {
            throwable.printStackTrace()
        }

        override fun onResponse(
            call: Call<String>,
            response: Response<String>
        ) {
            if (response.isSuccessful) {
                println(response.body())
            }
        }

    })
}

fun uploadFile(file: File, channelId: String, deleteFile: Boolean = true) {
    val requestBody =
        RequestBody.create(MediaType.parse(ServiceGenerator.MULTIPART_FORM_DATA), file)
    val multipartBody =
        MultipartBody.Part.createFormData("file", "App-debug.apk", requestBody)

    val appToken = RequestBody.create(
        okhttp3.MultipartBody.FORM,
        TOKEN!!
    )
    val title = RequestBody.create(okhttp3.MultipartBody.FORM, file.nameWithoutExtension)
    val filename = RequestBody.create(okhttp3.MultipartBody.FORM, file.name)
    val fileType = RequestBody.create(okhttp3.MultipartBody.FORM, "auto")
    val channels = RequestBody.create(okhttp3.MultipartBody.FORM, channelId)

    val api = ServiceGenerator.createService(RamukakaApi::class.java, SlackApi.BASE_URL, false)
    val call = api.pushApp(appToken, title, filename, fileType, channels, multipartBody)
    val response = call.execute()
    if (response.isSuccessful) {
        println(if (response.body()?.delivered == true) "delivered" else "Not delivered")
    } else {
        println(response.errorBody().toString())
    }
    if (deleteFile)
        file.delete()
}

@Location("/app")
data class Apk(val file: File)


