package com.tombspawn

import com.tombspawn.component.app.app
import kotlinx.browser.document
import react.dom.div
import react.dom.render

fun main() {
    kotlinext.js.require("../../../node_modules/semantic-ui-css/semantic.min.css")
    render(document.getElementById("root")) {
        div {
            app {

            }
        }
    }
}