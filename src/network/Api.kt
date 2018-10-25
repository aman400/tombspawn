package network

import com.ramukaka.Apk
import com.ramukaka.extensions.copyToSuspend
import com.ramukaka.extensions.execute
import com.ramukaka.extensions.random
import com.ramukaka.extensions.toMap
import com.ramukaka.models.ErrorResponse
import com.ramukaka.models.locations.Slack
import com.ramukaka.network.ServiceGenerator
import com.ramukaka.network.SlackApi
import io.ktor.application.call
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.locations.post
import io.ktor.request.receiveMultipart
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


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

fun Routing.status() {
    get("/") {
        call.respond(mapOf("status" to "OK"))
    }
}

fun Routing.health() {
    get("/health") {
        call.respond(mapOf("status" to "OK"))
    }
}

fun Routing.buildConsumer(gradlePath: String, uploadDirPath: String, consumerAppDir: String, token: String) {
    post<Slack.Consumer> {
        val params = call.receiveParameters()

        val channelId = params["channel_id"]
        val text = params["text"]
        val responseUrl = params["response_url"]
        val APKPrefix = System.currentTimeMillis()

        text?.trim()?.toMap()?.let { buildData ->
            var executableCommand =
                "$gradlePath assembleWithArgs -PFILE_PATH=$uploadDirPath -PAPP_PREFIX=$APKPrefix"

            buildData.forEach { key, value ->
                executableCommand += " -P$key=$value"
            }

            launch {
                println(executableCommand)
                val commandResponse = executableCommand.execute(File(consumerAppDir))

                val tempDirectory = File(uploadDirPath)
                if (tempDirectory.exists()) {
                    val firstFile = tempDirectory.listFiles { dir, name ->
                        name.contains("$APKPrefix", true)
                    }.firstOrNull()
                    firstFile?.let { file ->
                        if (file.exists()) {
                            uploadFile(file, channelId!!, token)
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
}


fun Routing.buildFleet(gradlePath: String, uploadDirPath: String, fleetAppDir: String, token: String) {
    post<Slack.Fleet> {
        val params = call.receiveParameters()

        val channelId = params["channel_id"]
        val text = params["text"]
        val APKPrefix = System.currentTimeMillis()
        val responseUrl = params["response_url"]

        text?.trim()?.toMap()?.let { buildData ->
            var executableCommand =
                "$gradlePath assembleWithArgs -PFILE_PATH=$uploadDirPath -PAPP_PREFIX=$APKPrefix"

            buildData.forEach { key, value ->
                executableCommand += " -P$key=$value"
            }

            launch {
                println(executableCommand)
                val commandResponse = executableCommand.execute(File(fleetAppDir))

                val tempDirectory = File(uploadDirPath)
                if (tempDirectory.exists()) {
                    val firstFile = tempDirectory.listFiles { dir, name ->
                        name.contains("$APKPrefix", true)
                    }.firstOrNull()
                    firstFile?.let { file ->
                        if (file.exists()) {
                            uploadFile(file, channelId!!, token)
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
}

fun Routing.receiveApk(uploadDirPath: String) {
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
                    val file = File(uploadDirPath, "upload-app.$ext")
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
}


private fun sendError(commandResponse: String?, responseUrl: String) {
    val errorResponse = if (!commandResponse.isNullOrEmpty()) {
        ErrorResponse(response = commandResponse)
    } else {
        ErrorResponse(response = "Something went wrong. Unable to generate APK.")
    }

    val api = ServiceGenerator.createService(SlackApi::class.java, SlackApi.BASE_URL, true)
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


private fun uploadFile(file: File, channelId: String, token: String, deleteFile: Boolean = true) {
    val requestBody =
        RequestBody.create(MediaType.parse(ServiceGenerator.MULTIPART_FORM_DATA), file)
    val multipartBody =
        MultipartBody.Part.createFormData("file", "App-debug.apk", requestBody)

    val appToken = RequestBody.create(
        okhttp3.MultipartBody.FORM,
        token
    )
    val title = RequestBody.create(okhttp3.MultipartBody.FORM, file.nameWithoutExtension)
    val filename = RequestBody.create(okhttp3.MultipartBody.FORM, file.name)
    val fileType = RequestBody.create(okhttp3.MultipartBody.FORM, "auto")
    val channels = RequestBody.create(okhttp3.MultipartBody.FORM, channelId)

    val api = ServiceGenerator.createService(SlackApi::class.java, SlackApi.BASE_URL, false)
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