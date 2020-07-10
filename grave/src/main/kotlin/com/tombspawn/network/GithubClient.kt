package com.tombspawn.network

import com.tombspawn.ApplicationService
import com.tombspawn.models.github.Payload
import com.tombspawn.models.locations.Apps
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.util.toMap

fun Routing.githubWebhook(applicationService: ApplicationService) {
    post<Apps.App.GithubWebhook> {
        val payload = call.receive<Payload>()
        val headers = call.request.headers.toMap()
        applicationService.handleGithubEvent(it.app.id, headers, payload)
        call.respond(HttpStatusCode.OK)
    }
}