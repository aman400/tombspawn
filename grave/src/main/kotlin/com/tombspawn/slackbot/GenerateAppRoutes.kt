package com.tombspawn.slackbot

import com.tombspawn.ApplicationService
import com.tombspawn.base.extensions.toMap
import com.tombspawn.data.Ref
import com.tombspawn.models.config.App
import com.tombspawn.models.locations.Slack
import com.tombspawn.models.slack.Element
import com.tombspawn.models.slack.ElementType
import com.tombspawn.models.slack.dialog
import com.tombspawn.models.slack.element
import com.tombspawn.utils.Constants
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.post
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing

fun Routing.buildApp(applicationService: ApplicationService) {
    post<Slack.Command> { command ->
        val params = call.receiveParameters()

        val channelId = params["channel_id"]
        val text = params["text"]
        val responseUrl = params["response_url"]
        val triggerId = params["trigger_id"]

        params.forEach { key, list ->
            LOGGER.info("$key: $list")
        }

        text?.trim()?.toMap()?.let { buildData ->
            applicationService.generateAndUploadApk(buildData, channelId!!, command.appID, responseUrl!!)
            call.respond(HttpStatusCode.OK)
        } ?: run {
            LOGGER.warn("Command options not set. These options can be set using '/build-fleet BRANCH=<git-branch-name>(optional)  BUILD_TYPE=<release/debug>(optional)  FLAVOUR=<flavour>(optional)'")
            applicationService.showGenerateApkDialog(command.appID, triggerId!!)
            call.respond(HttpStatusCode.OK)
        }
    }
}