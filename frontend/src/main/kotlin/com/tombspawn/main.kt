package com.tombspawn

import com.tombspawn.externals.semantic.ui.button.Button
import com.tombspawn.component.app.app
import kotlinx.browser.document
import react.dom.*

fun main() {
    kotlinext.js.require("../../../node_modules/semantic-ui-css/semantic.min.css")
    render(document.getElementById("root")) {
        div {
            app {

            }
        }
        Button {
            attrs.color = "red"
            attrs.active = true
            div { +"Hello" }
        }
    }
}