package com.tombspawn.component.app

import com.tombspawn.component.main.main
import react.*
import react.router.dom.browserRouter

external interface AppState : RState {
    var loggedIn: Boolean
}

external interface AppProps: RProps {

}

class App: RComponent<AppProps, AppState>() {
    override fun RBuilder.render() {
        browserRouter {
            main {  }
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