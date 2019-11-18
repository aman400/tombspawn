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

        @Location("/generate")
        data class CreateApp(val app: App)

        @Location("/branches")
        data class Branches(val app: App, val branchLimit: Int? = -1, val tagLimit: Int? = -1)

        @Location("/flavours")
        data class Flavours(val app: App)

        @Location("/generate")
        data class APK(val app: App)
    }
}