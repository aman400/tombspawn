package com.ramukaka.models.locations

import io.ktor.locations.Location

@Location("/slack/app")
class Slack {
    @Location("/command/consumer")
    class Consumer

    @Location("/command/fleet")
    class Fleet

    @Location("/command/{appID}")
    class Command(val appID: String) {
        @Location("/mock")
        class MockApi
    }

    @Location("/command/standup")
    class Standup

    @Location("/command/api/mock")
    class MockApi

    @Location("/subscribe/{appID}")
    class Subscribe(val appID: String)

    @Location("/event")
    class Event

    @Location("/action")
    class Action
}