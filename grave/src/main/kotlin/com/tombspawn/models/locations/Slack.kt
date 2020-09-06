package com.tombspawn.models.locations

import io.ktor.locations.*

@OptIn(KtorExperimentalLocationsAPI::class)
@Location("/slack/app")
class Slack {
    @Location("/command/{appID}")
    class Command(val appID: String, val slack: Slack)

    @Location("/event")
    class Event(val slack: Slack)

    @Location("/action")
    class Action(val slack: Slack)
}