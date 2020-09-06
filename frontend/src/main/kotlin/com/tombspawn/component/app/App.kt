package com.tombspawn.component.app

import com.tombspawn.component.home.home
import com.tombspawn.component.login.*
import com.tombspawn.component.redux.ApplicationState
import com.tombspawn.externals.axios.Axios
import com.tombspawn.externals.axios.AxiosRequestConfig
import kotlinext.js.jsObject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import react.*
import react.dom.div
import react.redux.rConnect
import react.router.dom.browserRouter
import react.router.dom.redirect
import react.router.dom.route
import react.router.dom.switch
import redux.WrapperAction

external interface AppProps: RProps {
    var user: User
    var onUserLogin: (email: String, password: String) -> Unit
    var onUserLogout: () -> Unit
}

external interface AppState: RState {
    val data: String
}

class App(props: AppProps): RComponent<AppProps, AppState>(props) {
    override fun RBuilder.render() {
        println(props.user.isLoggedIn)
        browserRouter {
            switch {
                route("/login", Login::class, exact = true)
//            route("/signup", Signup::class, exact = true)
                route<LoginProps>("/login/:id") { props ->
                    div {
                        +"User id: ${props.match.params.id}"
                    }
                }
                route("/home") {
                    home {
                        this.user = props.user
                    }
                }
                redirect(to = "/home")
            }
        }
    }
}

@Serializable
data class Message(val status: String)

private interface AppStateProps : RProps {
    var user: User
}

private interface AppDispatchProps : RProps {
    var onUserLogin: (email: String, password: String) -> Unit
    var onUserLogout: () -> Unit
}

val application: RClass<AppProps> =
    rConnect<ApplicationState, UserAction, WrapperAction, RProps, AppStateProps, AppDispatchProps, AppProps>(
        { state, ownProps ->
            this.user = state.user
        },
        { dispatch, ownProps ->
            onUserLogin = { email, password ->
                dispatch(LoginAction(email, password))
            }
            onUserLogout = {
                dispatch(LogoutAction)
            }
        }
    )(App::class.js.unsafeCast<RClass<AppProps>>())