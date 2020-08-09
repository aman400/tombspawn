package com.tombspawn.ui.app

import com.tombspawn.ui.appbar.appBar
import react.*

external interface AppState : RState {
    var loggedIn: Boolean
}

external interface AppProps: RProps {

}

class App: RComponent<AppProps, AppState>() {
    override fun RBuilder.render() {
        appBar {
            this.loggedIn = state.loggedIn
        }
    }

    override fun AppState.init() {
        setState {
            loggedIn = true
        }
    }
}

fun RBuilder.app(handler: AppProps.() -> Unit): ReactElement {
    return child(App::class) {
        this.attrs(handler)
    }
}