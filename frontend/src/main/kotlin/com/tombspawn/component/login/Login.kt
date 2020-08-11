package com.tombspawn.component.login

import react.*
import react.dom.div
import react.dom.h3

external interface LoginState : RState {
}

external interface LoginProps: RProps {
    var id: Int
}

class Login: RComponent<RProps, LoginState>() {
    override fun RBuilder.render() {
        div {
            h3 {
                +"Login screen"
            }
        }
    }
}

fun RBuilder.login(handler: RProps.() -> Unit): ReactElement {
    return child(Login::class) {
        this.attrs(handler)
    }
}