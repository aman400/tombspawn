package com.tombspawn.component.signup

import com.tombspawn.component.extensions.isJsNullOrEmpty
import com.tombspawn.component.utils.toUIError
import com.tombspawn.externals.semantic.ui.SemanticShorthandContent
import com.tombspawn.externals.semantic.ui.button.Button
import com.tombspawn.externals.semantic.ui.form.*
import com.tombspawn.externals.semantic.ui.grid.Grid
import com.tombspawn.externals.semantic.ui.grid.GridColumn
import com.tombspawn.externals.semantic.ui.message.Message
import com.tombspawn.externals.semantic.ui.others.Checkbox
import com.tombspawn.externals.semantic.ui.others.Input
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
import react.dom.body
import react.dom.div
import react.dom.h1
import kotlin.js.Json

external interface SignupState : RState {
    var firstName: String
    var lastName: String
    var email: String
    var password: String
}

external interface SignupProps : RProps {
    var id: Int
}

private val EMAIL_REGEX = Regex("[-0-9a-zA-Z.+_]+@[-0-9a-zA-Z.+_]+\\.[a-zA-Z]{2,4}")
private val PASSWORD_REGEX = Regex("^(?=.*[0-9a-zA-Z@\$!%*?&])([a-zA-Z0-9@\$!%*?&]{8,})\$")
private val NAME_REGEX = Regex("^(?=.*[a-zA-Z\\s])([a-zA-Z\\s]{3,})\$")

private const val EMAIL = "e-mail"
private const val PASSWORD = "password"
private const val FIRSTNAME = "firstname"
private const val LASTNAME = "lastname"

class Signup : RComponent<RProps, SignupState>() {

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
                FIRSTNAME -> {
                    this.firstName = data.value
                }
                LASTNAME -> {
                    this.lastName = data.value
                }
            }
        }
    }

    private fun validate(
        email: String?,
        password: String?,
        firstName: String?,
        lastName: String?
    ): MutableMap<String, Json?> {
        val error = mutableMapOf<String, Json?>()

        if (!email.isJsNullOrEmpty() && !EMAIL_REGEX.matches(email!!)) {
            error[EMAIL] = "Enter a valid email address".toUIError(FormError.Pointing.below)
        }

        if (!password.isJsNullOrEmpty() && !PASSWORD_REGEX.matches(password!!)) {
            error[PASSWORD] = "Password must be atleast 8 characters long and only contain [A-Z][a-z][0-9]@\$!%*?&".toUIError()
        }

        if(!firstName.isJsNullOrEmpty() && !NAME_REGEX.matches(firstName!!)) {
            error[FIRSTNAME] = "Enter a valid firstname".toUIError(FormError.Pointing.below)
        }

        if(!lastName.isJsNullOrEmpty() && !NAME_REGEX.matches(lastName!!)) {
            error[LASTNAME] = "Enter a valid lastname".toUIError(FormError.Pointing.below)
        }

        return error
    }

    override fun RBuilder.render() {
        val errors = validate(state.email, state.password, state.firstName, state.lastName)
        Grid {
            attrs.textAlign = "center"
            attrs.verticalAlign = "middle"
            attrs["style"] = js {
                height = 100.vh
            } as Any
            GridColumn {
                attrs["style"] = js {
                    maxWidth = 450.px
                } as Any
                Message {
                    attrs {
                        size = "large"
                        attached = true
                        header = Header {
                            attrs.`as` = "h4"
                            +"Welcome to Tombspawn!"
                        }
                        content = div {
                            +"Fill out the form below to sign-up for a new account"
                        }
                    }
                }
                loginForm(state, errors, ::onChange, ::onSubmit)
                Message {
                    attrs {
                        attached = "bottom"
                        warning = true
                    }
                    Icon {
                        attrs.name = "help"
                    }
                    +"Already signed up? "
                    a {
                        attrs.href = "/login"
                        +"com.tombspawn.component.login.Login here"
                    }
                    +" instead."
                }
            }
        }
    }
}

fun RBuilder.loginForm(
    state: SignupState,
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

                FormGroup {
                    attrs {
                        unstackable = true
                        widths = "equal"
                    }

                    FormInput {
                        attrs {
                            fluid = true
                            label = "First Name"
                            labelPosition = "left"
                            placeholder = "First Name"
                            type = InputType.text.realValue
                            error = errors[FIRSTNAME]
                            onChange = change
                            id = FIRSTNAME
                            set("name", FIRSTNAME)
                            set("value", state.firstName)
                            set("autoComplete", "on")
                        }
                    }

                    FormInput {
                        attrs {
                            fluid = true
                            label = "Last Name"
                            placeholder = "Last Name"
                            labelPosition = "left"
                            type = InputType.text.realValue
                            error = errors[LASTNAME]
                            onChange = change
                            id = LASTNAME
                            set("name", LASTNAME)
                            set("value", state.lastName)
                            set("autoComplete", "on")
                        }
                    }
                }

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
                FormCheckbox {
                    attrs {
                        inline = true
                        label = "I agree to the terms and conditions"
                    }
                }
                Button {
                    attrs {
                        fluid = true
                        color = "blue"
                        size = "large"
                        disabled =
                            errors.isNotEmpty() || state.email.isJsNullOrEmpty() || state.password.isJsNullOrEmpty()
                    }
                    +"com.tombspawn.component.login.Login"
                }
            }
        }
    }
}

fun RBuilder.signup(handler: RProps.() -> Unit): ReactElement {
    return child(Signup::class) {
        this.attrs(handler)
    }
}