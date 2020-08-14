package com.tombspawn.component.login

import com.tombspawn.component.skeleton.addAppForm
import react.*
import react.dom.div

external interface LoginState : RState {
}

external interface LoginProps: RProps {
    var id: Int
}

class Login: RComponent<RProps, LoginState>() {
    override fun RBuilder.render() {
        div {
            addAppForm {  }
        }
    }
}

fun RBuilder.login(handler: RProps.() -> Unit): ReactElement {
    return child(Login::class) {
        this.attrs(handler)
    }
}