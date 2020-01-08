package com.tombspawn.models.locations

import io.ktor.locations.Location

@Location("/apps")
class Apps {
    @Location("/{id}")
    data class App(val id: String) {
        @Location("/callback/{callbackId}")
        data class Callback(val app: App, val callbackId: String) {
            @Location("/success")
            class Success(val callback: Callback)
            @Location("/failure")
            class Failure(val callback: Callback)
        }

        @Location("/build-variants")
        data class BuildVariants(val app: App)

        @Location("/init")
        data class Init(val app: App)

        @Location("/generate")
        data class CreateApp(val app: App)

        @Location("/refs")
        data class References(val app: App)

        @Location("/flavours")
        data class Flavours(val app: App)

        @Location("/generate")
        data class APK(val app: App)
    }
}