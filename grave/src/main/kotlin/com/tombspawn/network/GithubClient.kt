package com.tombspawn.network

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
import org.slf4j.LoggerFactory

@Location("/github")
class GithubApi {

    @Location("/payload")
    class Webhook
}

val LOGGER = LoggerFactory.getLogger("com.tombspawn.network.GithubClient")

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