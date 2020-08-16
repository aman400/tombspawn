package com.tombspawn.component.main

import com.tombspawn.component.appbar.appBar
import com.tombspawn.component.home.Home
import com.tombspawn.component.login.Login
import com.tombspawn.component.login.LoginProps
import com.tombspawn.component.signup.Signup
import react.*
import react.dom.div
import react.router.dom.redirect
import react.router.dom.route
import react.router.dom.switch

external interface MainState : RState {
    var loggedIn: Boolean
}

external interface MainProps: RProps {

}

class Main: RComponent<MainProps, MainState>() {

    override fun RBuilder.render() {
        appBar {
            loggedIn = state.loggedIn
        }
        switch {
            route("/login", Login::class, exact = true)
            route("/signup", Signup::class, exact = true)
            route<LoginProps>("/login/:id") { props ->
                div {
                    +"User id: ${props.match.params.id}"
                }
            }
            route("/home", Home::class)
            redirect(to = "/login")
        }
    }
}

fun RBuilder.main(handler: MainProps.() -> Unit): ReactElement {
    return child(Main::class) {
        this.attrs(handler)
    }
}