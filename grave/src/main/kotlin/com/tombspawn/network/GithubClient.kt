package com.tombspawn.network

import com.tombspawn.ApplicationService
import com.tombspawn.models.Reference
import com.tombspawn.models.bitbucket.BitbucketResponse
import com.tombspawn.models.bitbucket.CommitData
import com.tombspawn.models.config.App
import com.tombspawn.models.github.Payload
import com.tombspawn.models.github.RefType
import com.tombspawn.models.locations.Apps
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.post
import io.ktor.request.*
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.util.toMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun Routing.githubWebhook(applicationService: ApplicationService) {
    post<Apps.App.GithubWebhook> {
        val payload = call.receive<Payload>()
        val headers = call.request.headers.toMap()
        applicationService.handleGithubEvent(it.app.id, headers, payload)
        call.respond(HttpStatusCode.OK)
    }

    post<Apps.App.BitbucketWebhook> {
        val body = call.receive<BitbucketResponse>()
        val headers = call.request.headers.toMap()
        applicationService.handleBitbucketEvent(it.app.id, headers, body)
        call.respond(HttpStatusCode.OK)
    }
}

fun CommitData?.getData(app: App?): Pair<App, Reference>? {
    return this?.let { data ->
        val refType = RefType.from(data.type)
        val name = data.name
        if(app != null && refType != null && name != null) {
            Pair(app, Reference(name, refType))
        } else null
    }
}