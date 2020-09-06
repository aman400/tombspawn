package com.tombspawn.component.redux

import com.tombspawn.component.extensions.combineReducers
import com.tombspawn.component.login.login

fun combinedReducers() = combineReducers(
    mapOf(
        ApplicationState::user to ::login,
//        "visibilityFilter" to ::visibilityFilter
    )
)