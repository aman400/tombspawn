package com.tombspawn.network

import com.tombspawn.session.models.LoginSession
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.sessions.*

fun Routing.status() {
    get("/") {
        call.request.queryParameters["email"]?.let { email ->
            call.sessions.set(LoginSession(email, "123456789"))
        }
        call.respond(mapOf("status" to "OK"))
    }
}

fun Routing.health() {
    get("/health") {
        val session = call.sessions.get<LoginSession>()
        call.respond(mapOf("status" to "email: ${session?.email}, token: ${session?.token}"))
    }
}