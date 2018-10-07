package com.ramukaka

import com.ramukaka.extensions.*
import com.ramukaka.network.RamukakaApi
import com.ramukaka.network.ServiceGenerator
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.locations.Location
import io.ktor.locations.Locations
import io.ktor.locations.post
import io.ktor.request.path
import io.ktor.request.receiveMultipart
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.coroutines.experimental.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.slf4j.event.Level
import java.io.File

fun main(args: Array<String>): Unit = io.ktor.server.netty.DevelopmentEngine.main(args)
private var UPLOAD_DIR_PATH = "/Users/aman/IdeaProjects/Ramukaka/public/"
private var GRADLE_PATH = "/usr/local/bin/gradle"
private var BUILD_TYPE = "release"
private var FLAVOUR = "flavour"
private var BRANCH = "master"
private var APP_DIR = "/Users/aman/git/LazySocket/"

private var DEFAULT_BRANCH = "master"
private var DEFAULT_BUILD_TYPE = "release"


private val randomWaitingMessages = listOf(
    "Try Holding your Breath",
    "Hold your horses",
    "Adding Randomly Mispeled Words Into Text",
    "Adding Vanilla Flavor to Ice Giants",
    "All races of Norrath will learn to work together",
    "Always Frisky Kerrans",
    "Attaching Beards to Dwarves",
    "Bristlebane Was Here",
    "Buy:LurN Tu Tok liK Da OgUr iN Ayt DaYz by OG",
    "Checking Anti-Camp Radius",
    "Creating Randomly Generated Feature",
    "Delivering the Lion Meat to Halas",
    "DING!",
    "Does Anyone Actually Read This?",
    "Doing Something You Don't Wanna Know About",
    "Doing The Impossible",
    "Don't Panic",
    "Dusting Off Spellbooks",
    "Ensuring Everything Works Perfektly",
    "Ensuring Gnomes Are Still Short",
    "Filling Halflings With Pie",
    "Generating Plans for Faster-Than-Light Travel",
    "Grrr. Bark. Bark. Grrr.",
    "Have You Hugged An Iksar Today?",
    "Have You Tried Batwing Crunchies Cereal?",
    "Hiding Catnip From Vah Shir",
    "Hitting Your Keyboard Won't Make This Faster",
    "Honk if You Eat Gnomes",
    "If You Squeeze Dark Elves You Don't Get Wine",
    "In The Grey, No One Can Hear You Scream",
    "Isn't It About Time You Washed Your Armor?",
    "Karnor's... Over 41 Billion Trains Served",
    "Loading, Don't Wait If You Don't Want To",
    "Look Out Behind You",
    "Looking For Graphics",
    "Looking Up Barbarian Kilts",
    "Now Spawning Fippy_Darkpaw_432,366,578",
    "Oiling Clockworks",
    "Outfitting Pigs With Wings",
    "Pizza...The Other Other White Meat",
    "Polishing Erudite Foreheads",
    "Preparing to Spin You Around Rapidly",
    "Refreshing Death Touch Ammunition",
    "Ruining My Own Lands",
    "Sanding Wood Elves... now 34% smoother.",
    "Sharpening Claws",
    "Sharpening Swords",
    "Spawning Your_Characters01",
    "Starching High Elf Robes",
    "Stringing Bows",
    "Stupidificationing Ogres",
    "Teaching Snakes to Kick",
    "Told You It Wasn't Made of Cheese",
    "Warning: Half Elves Are Now .49999 Elves.",
    "Whacking Trolls With Ugly Stick",
    "Why Do Clerics Always Burn the Bread?",
    "Wonder If Phinegal Autropos Tires of Seafood?",
    "You Have Gotten Better At Fizzling! (47)"
)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val loadingMessages =
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

    routing {
        post<App> {
            val params = call.receiveParameters()
            params.entries().forEach { param ->
                println("${param.key}, ${param.value}")
            }

            val token = params["token"]
            val channelId = params["channel_id"]
            val channelName = params["channel_name"]
            val userName = params["user_name"]
            val userId = params["user_id"]
            val text = params["text"]

            if (!text.isNullOrEmpty()) {
                text?.let { textCommand ->
                    val buildData = textCommand.toMap()

                    var executableCommand = "$GRADLE_PATH assembleWithArgs -PFILE_PATH=$UPLOAD_DIR_PATH"

                    val branch = buildData["BRANCH"] ?: DEFAULT_BRANCH
                    executableCommand += " -PBRANCH=$branch"

                    val flavor = buildData["FLAVOUR"]

                    if(flavor != null) {
                        executableCommand += " -PFLAVOUR=$flavor"
                    }

                    val buildType = buildData["TYPE"] ?: DEFAULT_BUILD_TYPE
                    executableCommand += " -PBUILD_TYPE=$buildType"

                    launch {
                        println(executableCommand)
                        executableCommand.execute(
                            File(APP_DIR)
                        )

                        val tempDirectory = File(UPLOAD_DIR_PATH)
                        val file = tempDirectory.listFiles { dir, name ->
                            println(name)
                            name.endsWith("apk", true)
                        }.first()
                        if (file.exists()) {
                            val requestBody =
                                RequestBody.create(MediaType.parse(ServiceGenerator.MULTIPART_FORM_DATA), file)
                            val multipartBody =
                                MultipartBody.Part.createFormData("file", "App-debug.apk", requestBody)

                            val appToken = RequestBody.create(
                                okhttp3.MultipartBody.FORM,
                                "xoxb-445373233521-449196750276-dX1OtiZmEgl2ktQydRomlJ4f"
                            )
                            val title = RequestBody.create(okhttp3.MultipartBody.FORM, file.nameWithoutExtension)
                            val filename = RequestBody.create(okhttp3.MultipartBody.FORM, file.name)
                            val fileType = RequestBody.create(okhttp3.MultipartBody.FORM, "auto")
                            val channels = RequestBody.create(okhttp3.MultipartBody.FORM, channelName!!)

                            val api = ServiceGenerator.createService(RamukakaApi::class.java, false)
                            val call = api.pushApp(appToken, title, filename, fileType, channels, multipartBody)
                            val response = call.execute()
                            if (response.isSuccessful) {
                                println(if (response.body()?.delivered == true) "delivered" else "Not delivered")
                            } else {
                                println(response.errorBody().toString())
                            }
                            tempDirectory.cleanup()
                        }

                    }
                    call.respond(randomWaitingMessages.random()!!)
                }
            } else {
                call.respond("Invalid build command use '/build consumer <url>'. <url> is optional parameter.")
            }
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
                        val file = File("/Users/aman/IdeaProjects/Ramukaka/public", "upload-app.$ext")
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

        get("/") {
        }
    }
}

@Location("/app")
data class Apk(val file: File)

@Location("/")
data class App(val url: String = "")


