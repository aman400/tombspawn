package com.ramukaka.models.locations

import io.ktor.locations.Location

@Location("/github")
class GithubApi {

    @Location("/payload/")
    class Webhook
}