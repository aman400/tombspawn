package com.ramukaka.network

import com.ramukaka.data.Branches
import com.ramukaka.data.Database
import com.ramukaka.data.Subscriptions
import com.ramukaka.data.Users
import com.ramukaka.models.github.RefType
import com.ramukaka.utils.Constants
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import models.github.Payload

@Location("/github")
class GithubApi {

    @Location("/payload")
    class Webhook
}

fun Routing.githubWebhook(database: Database, slackClient: SlackClient) {
    post<GithubApi.Webhook> {
        val payload = call.receive<Payload>()
        val headers = call.request.headers
        when {
            headers[Constants.Github.HEADER_KEY_EVENT] == Constants.Github.HEADER_VALUE_EVENT_PUSH -> payload.ref?.let { ref ->
                val branch = ref.substringAfter("refs/heads/")
                val subscriptions = database.findSubscriptions(branch)
                subscriptions.orEmpty().forEach { resultRow ->
                    launch(Dispatchers.IO) {
                        slackClient.sendShowConfirmGenerateApk(
                            resultRow[Subscriptions.channel],
                            resultRow[Branches.name],
                            resultRow[Users.slackId]
                        )
                    }
                }
                if (branch == "development") {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond("Not development branch")
                }
            } ?: call.respond("Not development branch")
            headers[Constants.Github.HEADER_KEY_EVENT] == Constants.Github.HEADER_VALUE_EVENT_CREATE -> {
                call.respond(HttpStatusCode.OK)
                launch {
                    if(payload.refType!! == RefType.BRANCH) {
                        database.addBranch(payload.ref!!, Constants.Common.APP_CONSUMER)
                    }
                }
            }
            headers[Constants.Github.HEADER_KEY_EVENT] == Constants.Github.HEADER_VALUE_EVENT_DELETE -> {
                call.respond(HttpStatusCode.OK)
                database.deleteBranch(payload.ref!!)
                LOGGER.info("deleted branch: ${payload.ref}")
            }

            headers[Constants.Github.HEADER_KEY_EVENT] == Constants.Github.HEADER_VALUE_EVENT_PING -> {
                call.respond(HttpStatusCode.OK)
            }

            else -> {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}