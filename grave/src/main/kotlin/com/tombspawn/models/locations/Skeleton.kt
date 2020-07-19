package com.tombspawn.models.locations

import io.ktor.locations.Location

@Location("/apps")
class Apps {
    @Location("/{id}")
    data class App(val id: String) {

        @Location("/github/payload")
        class GithubWebhook(val app: App)

        @Location("/bitbucket/payload")
        class BitbucketWebhook(val app: App)

        @Location("/init")
        data class Init(val app: App)

        @Location("/clean")
        data class Clean(val app: App)
    }

    @Location("/subscribe")
    class Subscribe

    @Location("/unsubscribe")
    class Unsubscribe
}