package com.tombspawn.component.login

import redux.RAction

sealed class UserAction: RAction
class LoginAction(val email: String, val password: String): UserAction()
object LogoutAction : UserAction()

data class User(val email: String? = null, val isLoggedIn: Boolean = false)

fun login(state: User = User(), action: RAction): User = when (action) {
    is LoginAction -> User(action.email, true)
    is LogoutAction -> state.copy(isLoggedIn = false)
    else -> state
}