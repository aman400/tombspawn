package com.tombspawn.component.home

import com.tombspawn.component.app.Message
import com.tombspawn.component.appbar.appBar
import com.tombspawn.component.login.User
import com.tombspawn.externals.axios.Axios
import kotlinext.js.jsObject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import react.*
import react.dom.div
import react.dom.h4

external interface HomeState : RState {
}

external interface HomeProps: RProps {
    var user: User
}

class Home(props: HomeProps): RComponent<HomeProps, HomeState>(props) {
    override fun RBuilder.render() {
        appBar {
//            loggedIn = state.loggedIn
        }
        div {
            h4 {
                if (props.user.isLoggedIn) {
                    +"Logged in"
                } else {
                    +"Logged out"
                }
            }
        }
    }


    override fun componentDidMount() {
        val mainScope = MainScope()

        mainScope.launch {
            Axios.get<Message>("http://localhost:8080/health", jsObject {
                withCredentials = true
            }).await().let {
                println(it.data.status)
            }
        }
    }
}

fun RBuilder.home(handler: HomeProps.() -> Unit): ReactElement {
    return child(Home::class) {
        this.attrs(handler)
    }
}