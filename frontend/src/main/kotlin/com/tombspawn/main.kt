package com.tombspawn

import com.tombspawn.component.app.application
import com.tombspawn.component.login.User
import com.tombspawn.component.redux.ApplicationState
import com.tombspawn.component.redux.combinedReducers
import kotlinx.browser.document
import react.dom.div
import react.dom.render
import react.redux.provider
import redux.RAction
import redux.compose
import redux.createStore
import redux.rEnhancer


val store = createStore<ApplicationState, RAction, dynamic>(
    combinedReducers(),
    ApplicationState(), compose(
        rEnhancer(),
        js("if(window.__REDUX_DEVTOOLS_EXTENSION__ )window.__REDUX_DEVTOOLS_EXTENSION__ ();else(function(f){return f;});")
    )
)

fun main() {
    kotlinext.js.require("../../../node_modules/semantic-ui-css/semantic.min.css")
    render(document.getElementById("root")) {
        provider(store) {
            div {
                application {
                    this.attrs.user = User("amandeep400@gmail.com", true)
                }
            }
        }
    }
}