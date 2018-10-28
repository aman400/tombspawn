package com.ramukaka.network

import com.ramukaka.utils.Constants
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import models.github.Payload

@Location("/github")
class GithubApi {

    @Location("/payload/")
    class Webhook
}

fun Routing.githubWebhook() {
    post<GithubApi.Webhook> {
        val payload = call.receive<Payload>()
        val headers = call.request.headers
        when {
            headers[Constants.Github.HEADER_KEY_EVENT] == Constants.Github.HEADER_VALUE_EVENT_PUSH -> payload.ref?.let { ref ->
                val branch = ref.substringAfter("refs/heads/")
                if (branch == "development") {
                    call.respond("OK")
                } else {
                    call.respond("Not development branch")
                }
            } ?: call.respond("Not development branch")
            headers[Constants.Github.HEADER_KEY_EVENT] == Constants.Github.HEADER_VALUE_EVENT_CREATE -> {
                call.respond("")
                println("Branch created")
            }
            headers[Constants.Github.HEADER_KEY_EVENT] == Constants.Github.HEADER_VALUE_EVENT_DELETE -> {
                println("Branch deleted")
            }
        }
    }
}