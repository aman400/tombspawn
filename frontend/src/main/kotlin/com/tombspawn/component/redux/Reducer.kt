package com.tombspawn.component.redux

import com.tombspawn.component.login.User

data class ApplicationState(
    val user: User = User(null, false)
)