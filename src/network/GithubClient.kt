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

fun Routing.githubWebhook(database: Database, slackClient: SlackClient, consumerAppID: String, fleetAppId: String) {
    post<GithubApi.Webhook> {
        val payload = call.receive<Payload>()
        val headers = call.request.headers
        when {
            headers[Constants.Github.HEADER_KEY_EVENT] == Constants.Github.HEADER_VALUE_EVENT_PUSH -> payload.ref?.let { ref ->
                call.respond(HttpStatusCode.OK)
                val branch = ref.substringAfter("refs/heads/")
                val subscriptions = database.findSubscriptions(branch)
                subscriptions.orEmpty().forEach { resultRow ->
                    launch(Dispatchers.IO) {
                        slackClient.sendShowConfirmGenerateApk(
                            resultRow[Subscriptions.channel],
                            resultRow[Branches.name]
                        )
                    }
                }
            }
            headers[Constants.Github.HEADER_KEY_EVENT] == Constants.Github.HEADER_VALUE_EVENT_CREATE -> {
                call.respond(HttpStatusCode.OK)
                launch {
                    if (payload.refType!! == RefType.BRANCH) {
                        when (payload.repository!!.id!!) {
                            consumerAppID -> {
                                database.addBranch(payload.ref!!, Constants.Common.APP_CONSUMER)
                            }
                            fleetAppId -> {
                                database.addBranch(payload.ref!!, Constants.Common.APP_FLEET)
                            }
                        }
                    }
                }
            }
            headers[Constants.Github.HEADER_KEY_EVENT] == Constants.Github.HEADER_VALUE_EVENT_DELETE -> {
                call.respond(HttpStatusCode.OK)
                launch {
                    if (payload.refType!! == RefType.BRANCH) {
                        when (payload.repository!!.id!!) {
                            consumerAppID -> {
                                database.deleteBranch(payload.ref!!, Constants.Common.APP_CONSUMER)
                            }
                            fleetAppId -> {
                                database.deleteBranch(payload.ref!!, Constants.Common.APP_FLEET)
                            }
                        }
                    }
                }
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