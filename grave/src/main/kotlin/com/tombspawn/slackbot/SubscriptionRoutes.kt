@file:JvmName("Subscription")
package com.tombspawn.slackbot

import com.tombspawn.ApplicationService
import com.tombspawn.models.locations.Slack
import com.tombspawn.utils.Constants
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.post
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger("com.tombspawn.slackbot.Subscription")

fun Routing.subscribe(applicationService: ApplicationService) {
    post<Slack.Subscribe> { subscription ->
        LOGGER.debug(subscription.toString())
        val parameters = call.receiveParameters()
        val triggerId = parameters[Constants.Slack.TRIGGER_ID]
        applicationService.showSubscriptionDialog(subscription.appID, triggerId!!)
        call.respond(HttpStatusCode.OK)
    }
}