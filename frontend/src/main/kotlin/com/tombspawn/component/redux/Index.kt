package com.tombspawn.component.redux

import com.tombspawn.component.extensions.combineReducers
import com.tombspawn.component.login.LoginState
import com.tombspawn.component.login.login

fun combinedReducers() = combineReducers(
    mapOf(
        LoginState::user to ::login,
//        "visibilityFilter" to ::visibilityFilter
    )
)