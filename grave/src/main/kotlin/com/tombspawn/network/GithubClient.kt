package com.tombspawn.network

import com.tombspawn.ApplicationService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import com.tombspawn.models.github.Payload
import io.ktor.util.toMap

@Location("/github")
class GithubApi {

    @Location("/payload")
    class Webhook
}

fun Routing.githubWebhook(applicationService: ApplicationService) {
    post<GithubApi.Webhook> {
        val payload = call.receive<Payload>()
        val headers = call.request.headers.toMap()
        applicationService.handleGithubEvent(headers, payload)
        call.respond(HttpStatusCode.OK)
    }
}