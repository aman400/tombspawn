package com.tombspawn.component.appbar

import com.tombspawn.externals.semantic.ui.button.Button
import com.tombspawn.externals.semantic.ui.menu.Menu
import com.tombspawn.externals.semantic.ui.menu.MenuItem
import com.tombspawn.externals.semantic.ui.menu.MenuMenu
import kotlinx.html.NAV
import org.w3c.dom.Navigator
import react.*
import react.router.dom.LinkComponent
import react.router.dom.routeLink

external interface AppBarProps: RProps {
    var loggedIn: Boolean
}

class AppBarState: RState {
}

class AppBar: RComponent<AppBarProps, AppBarState>() {
    override fun RBuilder.render() {
        Menu {
            this.attrs.className = "secondary"
            MenuItem {
                this.attrs.name = "home"
                routeLink("/home", true) {
                    +"Home"
                }
            }
            MenuItem {
                this.attrs.name = "login"
                routeLink("/login", true) {
                    +"Login"
                }
            }
            MenuMenu {
                this.attrs.position = "right"
                MenuItem {
                    if (props.loggedIn) {
                        Button {
                            this.attrs.className = "positive"
                            +"Login"
                        }
                    } else {
                        Button {
                            this.attrs.className = "negative"
                            +"Logout"
                        }
                    }
                }
            }
        }
    }

    override fun AppBarState.init() {
        setState {

        }
    }
}

fun RBuilder.appBar(handler: AppBarProps.() -> Unit): ReactElement {
    return child(AppBar::class) {
        this.attrs(handler)
    }
}