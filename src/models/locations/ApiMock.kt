package com.ramukaka.models.locations

import io.ktor.locations.Location

@Location("/api/mock")
class ApiMock {
    @Location("/{apiId}")
    data class GeneratedApi(val apiId: String)
}