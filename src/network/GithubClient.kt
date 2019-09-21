package com.ramukaka.network

import com.ramukaka.data.Database
import com.ramukaka.data.Refs
import com.ramukaka.data.Subscriptions
import com.ramukaka.models.Reference
import com.ramukaka.models.config.App
import com.ramukaka.models.github.RefType
import com.ramukaka.slackbot.SlackClient
import com.ramukaka.slackbot.sendShowConfirmGenerateApk
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
import org.slf4j.LoggerFactory

@Location("/github")
class GithubApi {

    @Location("/payload")
    class Webhook
}

val LOGGER = LoggerFactory.getLogger("com.ramukaka.network.GithubClient")

fun Routing.githubWebhook(apps: List<App>, database: Database, slackClient: SlackClient) {
    post<GithubApi.Webhook> {
        val payload = call.receive<Payload>()
        val headers = call.request.headers
        when {
            headers[Constants.Github.HEADER_KEY_EVENT] == Constants.Github.HEADER_VALUE_EVENT_PUSH -> payload.ref?.let { ref ->
                apps.firstOrNull {
                    it.repoId == payload.repository?.id
                }?.let { app ->
                    call.respond(HttpStatusCode.OK)
                    val branch = ref.substringAfter("refs/heads/")
                    val subscriptions = database.findSubscriptions(branch, app.id)
                    subscriptions.orEmpty().forEach { resultRow ->
                        launch(Dispatchers.IO) {
                            slackClient.sendShowConfirmGenerateApk(
                                resultRow[Subscriptions.channel],
                                resultRow[Refs.name],
                                Constants.Slack.CALLBACK_CONFIRM_GENERATE_APK + app.id
                            )
                        }
                    }
                }
            }
            headers[Constants.Github.HEADER_KEY_EVENT] == Constants.Github.HEADER_VALUE_EVENT_CREATE -> {
                call.respond(HttpStatusCode.OK)
                launch {
                    if (payload.refType == RefType.BRANCH) {
                        apps.firstOrNull {
                            it.repoId == payload.repository?.id
                        }?.let { app ->
                            payload.ref?.let { ref ->
                                database.addRef(app.id, Reference(ref, RefType.BRANCH))
                            }
                        }
                    } else if (payload.refType == RefType.TAG) {
                        apps.firstOrNull {
                            it.repoId == payload.repository?.id
                        }?.let { app ->
                            payload.ref?.let { ref ->
                                database.addRef(app.id, Reference(ref, RefType.TAG))
                            }
                        }
                    }
                }
            }
            headers[Constants.Github.HEADER_KEY_EVENT] == Constants.Github.HEADER_VALUE_EVENT_DELETE -> {
                call.respond(HttpStatusCode.OK)
                launch {
                    if (payload.refType == RefType.BRANCH) {
                        apps.firstOrNull {
                            it.repoId == payload.repository?.id
                        }?.let { app ->
                            payload.ref?.let { ref ->
                                database.deleteRef(app.id, Reference(ref, RefType.BRANCH))
                            }
                        }
                    } else if (payload.refType == RefType.TAG) {
                        apps.firstOrNull {
                            it.repoId == payload.repository?.id
                        }?.let { app ->
                            payload.ref?.let { ref ->
                                database.deleteRef(app.id, Reference(ref, RefType.TAG))
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