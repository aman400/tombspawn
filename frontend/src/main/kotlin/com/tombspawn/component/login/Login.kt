package com.tombspawn.component.login

import com.tombspawn.component.utils.toUIError
import com.tombspawn.externals.semantic.ui.button.Button
import com.tombspawn.externals.semantic.ui.form.Form
import com.tombspawn.externals.semantic.ui.form.FormError
import com.tombspawn.externals.semantic.ui.form.FormInput
import com.tombspawn.externals.semantic.ui.form.FormProps
import com.tombspawn.externals.semantic.ui.grid.Grid
import com.tombspawn.externals.semantic.ui.grid.GridColumn
import com.tombspawn.externals.semantic.ui.message.Message
import com.tombspawn.externals.semantic.ui.others.InputOnChangeData
import com.tombspawn.externals.semantic.ui.others.header.Header
import com.tombspawn.externals.semantic.ui.others.icon.Icon
import com.tombspawn.externals.semantic.ui.others.segment.Segment
import kotlinext.js.js
import kotlinx.css.px
import kotlinx.css.vh
import kotlinx.html.InputType
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.*
import react.dom.a
import kotlin.js.Json

external interface LoginState : RState {
    var email: String
    var password: String
}

external interface LoginProps : RProps {
    var id: Int
}

private val EMAIL_REGEX = Regex("[-0-9a-zA-Z.+_]+@[-0-9a-zA-Z.+_]+\\.[a-zA-Z]{2,4}")

private const val EMAIL = "e-mail"
private const val PASSWORD = "password"

class Login : RComponent<RProps, LoginState>() {

    private fun onSubmit(event: Event, props: FormProps) {
        println("${state.email} ${state.password}")
        event.preventDefault()
    }

    private fun onChange(event: Event, data: InputOnChangeData) {
        val target = (event.target as HTMLInputElement)
        setState {
            when (target.name) {
                EMAIL -> {
                    this.email = data.value
                }
                PASSWORD -> {
                    this.password = data.value
                }
            }
        }
    }

    private fun validate(email: String?, password: String?): MutableMap<String, Json?> {
        val error = mutableMapOf<String, Json?>()

        if (!email.isNullOrEmpty() && !EMAIL_REGEX.matches(email)) {
            error[EMAIL] = "Enter a valid email address".toUIError(FormError.Pointing.below)
        }

        if (!password.isNullOrEmpty() && password.length < 3) {
            error[PASSWORD] = "Enter a valid password".toUIError()
        }

        return error
    }

    override fun RBuilder.render() {
        val errors = validate(state.email, state.password)
        Grid {
            attrs.textAlign = "center"
            attrs.verticalAlign = "middle"
            attrs["style"] = js {
                height = 100.vh
            } as Any
            GridColumn {
                attrs["style"] = js {
                    maxWidth = 400.px
                } as Any
                Header {
                    attrs.`as` = "h2"
                    attrs.color = "blue"
                    attrs.textAlign = "center"
                    Icon {
                        attrs.name = "truck"
                        attrs.size = "mini"
                    }
                    +"Log-in to your account"
                }
                loginForm(state, errors, ::onChange, ::onSubmit)
                Message {
                    +"New to us? "
                    a {
                        attrs.href = "#"
                        +"Signup"
                    }
                }
            }
        }
    }
}

fun RBuilder.loginForm(
    state: LoginState,
    errors: Map<String, Json?>,
    change: (Event, InputOnChangeData) -> Unit,
    submit: (Event, FormProps) -> Unit
): ReactElement {
    return Form {
        attrs {
            size = "large"
            onSubmit = submit
        }
        Segment {
            attrs {
                stacked = true
                FormInput {
                    attrs {
                        fluid = true
                        icon = "user"
                        iconPosition = "left"
                        placeholder = "E-mail Address"
                        type = InputType.email.realValue
                        error = errors[EMAIL]
                        id = EMAIL
                        onChange = change
                        set("name", EMAIL)
                        set("value", state.email)
                        set("autoComplete", "on")
                    }
                }

                FormInput {
                    attrs {
                        fluid = true
                        icon = "lock"
                        iconPosition = "left"
                        placeholder = "Password"
                        type = InputType.password.realValue
                        error = errors[PASSWORD]
                        id = PASSWORD
                        onChange = change
                        set("name", PASSWORD)
                        set("value", state.password)
                        set("autoComplete", "on")
                    }
                }
                Button {
                    attrs {
                        fluid = true
                        color = "blue"
                        size = "large"
                    }
                    +"Login"
                }
            }
        }
    }
}

fun RBuilder.login(handler: RProps.() -> Unit): ReactElement {
    return child(Login::class) {
        this.attrs(handler)
    }
}