package com.tombspawn.slackbot

import com.tombspawn.ApplicationService
import com.tombspawn.models.locations.Slack
import com.tombspawn.models.slack.Event
import com.tombspawn.models.slack.SlackEvent
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import org.slf4j.LoggerFactory

val LOGGER = LoggerFactory.getLogger("com.application.slack.routing")


fun Routing.slackEvent(applicationService: ApplicationService) {
    post<Slack.Event> {
        val slackEvent = call.receive<SlackEvent>()
        println(slackEvent.toString())
        when (slackEvent.type) {
            Event.EventType.URL_VERIFICATION -> call.respond(slackEvent)
            Event.EventType.RATE_LIMIT -> {
                call.respond(HttpStatusCode.OK)
                LOGGER.error("Slack Api Rate Limit")
                println("Api rate limit")
            }
            Event.EventType.EVENT_CALLBACK -> {
                call.respond(HttpStatusCode.OK)
                applicationService.subscribeSlackEvent(slackEvent)
            }
            else -> {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

fun Routing.slackAction(
    applicationService: ApplicationService
) {
    post<Slack.Action> {
        val params = call.receive<Parameters>()
        val payload = params["payload"]
        println(payload)
        payload?.let {
            applicationService.handleSlackEvent(it)
        }
        call.respond(HttpStatusCode.OK)
    }
}