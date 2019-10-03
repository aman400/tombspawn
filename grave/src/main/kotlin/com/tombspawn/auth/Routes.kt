package com.tombspawn.auth

import io.ktor.locations.Location

@Location("/api")
class Api {

    @Location("/slack")
    class Slack {

        @Location("/verify")
        class Verify

        @Location("/login")
        class Login(val code: String? = null)
    }

    @Location("/logout")
    class Logout

    @Location("/users")
    class Users {
        @Location("/{userId}")
        class User(val userId: String)
    }

    @Location("users/{userId}/standups")
    class Standups(val userId: String)

    @Location("users/{userId}/standups/{standupId}")
    class Standup(val userId: String, val standupId: String)
}