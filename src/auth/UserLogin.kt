package com.ramukaka.auth

import com.ramukaka.auth.sessions.SlackSession
import com.ramukaka.data.Database
import com.ramukaka.data.DBUser
import com.ramukaka.models.SuccessResponse
import com.ramukaka.slackbot.SlackClient
import com.ramukaka.userdata.users.User
import com.ramukaka.utils.Constants
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.locations.delete
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import kotlinx.html.*
import kotlinx.html.stream.createHTML

fun Routing.auth(slackClient: SlackClient, jwtConfig: JWTConfig, database: Database) {
    get<Api.Slack.Login> { verifyData ->
        println(verifyData.code.toString())
        verifyData.code?.let {
            println(call.sessions.get(Constants.Slack.SESSION))
            slackClient.verifySlackAuth(it)?.let { user ->
                val session = SlackSession(jwtConfig.createToken(user))
                println(session.token)
                call.sessions.set(Constants.Slack.SESSION, session)
                call.respondText(getHtml(), ContentType.Text.Html, HttpStatusCode.OK)
            } ?: run {
                call.respond(HttpStatusCode.Forbidden)
            }
        }
    }

    authenticate("slack-auth") {
        delete<Api.Logout> {
            call.sessions.clear(Constants.Slack.SESSION)
            call.respond(HttpStatusCode.OK, SuccessResponse("Logged out successfully."))
        }
    }

    authenticate("slack-auth") {
        get<Api.Slack.Verify> {
            var user = call.authentication.principal<DBUser>()
            if(user == null) {
                val session = call.sessions.get<SlackSession>()
                user = database.findUser(jwtConfig.verifier.verify(session?.token).getClaim("id").asString())
            }

            call.respond(HttpStatusCode.OK, user?.let {
                "{\"email\": ${it.email}}"
            } ?: "{}")
        }
    }
}

fun Routing.users(database: Database) {
    get<Api.Users> {
        call.respond(database.getUsers(Constants.Database.USER_TYPE_USER).map {
            User(it.slackId, it.name, it.email)
        })
    }
    authenticate("slack-auth") {
        get<Api.Standups> {
            call.respond("{}")
        }

        post<Api.Standups> {
            call.respond("{}")
        }
    }
}

fun getHtml(): String {
    return createHTML(true).html {
        head {
            script(type = ScriptType.textJavaScript) {
                unsafe {
                    raw(
                        """
                        window.opener.postMessage("logged in successfully", "http://localhost:3000");
                        window.close();
                    """
                    )
                }
            }
        }
    }
}
