package com.tombspawn.network

import com.tombspawn.ApplicationService
import com.tombspawn.data.Database
import com.tombspawn.data.Refs
import com.tombspawn.data.Subscriptions
import com.tombspawn.models.Reference
import com.tombspawn.models.config.App
import com.tombspawn.models.github.RefType
import com.tombspawn.slackbot.SlackClient
import com.tombspawn.slackbot.sendShowConfirmGenerateApk
import com.tombspawn.utils.Constants
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.tombspawn.models.github.Payload
import io.ktor.util.toMap
import org.slf4j.LoggerFactory

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