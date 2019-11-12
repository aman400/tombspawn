package com.tombspawn.models.locations

import io.ktor.locations.Location

@Location("/apps")
class Apps {
    @Location("/{id}")
    data class App(val id: String) {
        @Location("/branches")
        data class Branches(val app: App, val branchLimit: Int? = -1, val tagLimit: Int? = -1)

        @Location("/flavours")
        data class Flavours(val app: App)

        @Location("/generate")
        data class APK(val app: App)
    }
}