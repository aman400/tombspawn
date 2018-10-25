package com.ramukaka.models.locations

import io.ktor.locations.Location

@Location("/slack/app")
class Slack {
    @Location("/command/consumer")
    class Consumer

    @Location("/command/fleet")
    class Fleet

    @Location("/subscribe")
    class Subscribe

    @Location("/event")
    class Event

    @Location("/action")
    class Action
}