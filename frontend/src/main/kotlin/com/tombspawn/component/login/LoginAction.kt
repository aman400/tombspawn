package com.tombspawn.component.login

import redux.RAction

class LoginAction(val email: String, val password: String): RAction
class LogoutAction(val email: String): RAction

data class User(val email: String? = null, val isLoggedIn: Boolean = false)

fun login(state: User = User(), action: RAction): User = when (action) {
    is LoginAction -> User(action.email, true)
    is LogoutAction -> state.copy(isLoggedIn = false)
    else -> state
}