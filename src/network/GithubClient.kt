package com.ramukaka.network

import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import models.Payload

@Location("/github")
class GithubApi {

    @Location("/payload/")
    class Webhook
}

fun Routing.githubWebhook() {
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