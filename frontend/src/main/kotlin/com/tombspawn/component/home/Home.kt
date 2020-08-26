package com.tombspawn.component.home

import com.tombspawn.component.appbar.appBar
import react.*
import react.dom.div
import react.dom.h4

external interface HomeState : RState {
}

external interface HomeProps: RProps {

}

class Home: RComponent<RProps, HomeState>() {
    override fun RBuilder.render() {
        appBar {
//            loggedIn = state.loggedIn
        }
        div {
            h4 {
                +"Home"
            }
        }
    }
}


fun RBuilder.home(handler: RProps.() -> Unit): ReactElement {
    return child(Home::class) {
        this.attrs(handler)
    }
}