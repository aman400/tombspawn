package com.tombspawn.models.locations

import io.ktor.locations.Location

@Location("/slack/app")
class Slack {
    @Location("/command/{appID}")
    class Command(val appID: String) {
    }

    @Location("/event")
    class Event

    @Location("/action")
    class Action
}