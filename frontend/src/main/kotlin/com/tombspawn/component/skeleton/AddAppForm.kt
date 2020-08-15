package com.tombspawn.component.skeleton

import com.tombspawn.component.extensions.ifNullOrEmpty
import com.tombspawn.component.utils.toUIError
import com.tombspawn.externals.semantic.ui.form.*
import com.tombspawn.externals.semantic.ui.grid.Grid
import com.tombspawn.externals.semantic.ui.grid.GridColumn
import com.tombspawn.externals.semantic.ui.grid.GridRow
import com.tombspawn.externals.semantic.ui.others.Input
import com.tombspawn.externals.semantic.ui.others.InputOnChangeData
import com.tombspawn.externals.semantic.ui.others.segment.Segment
import com.tombspawn.externals.semantic.ui.others.segment.SegmentGroup
import kotlinx.html.InputType
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.*
import react.dom.h4
import kotlin.js.Json

private const val GIT_USERNAME = "git-username"
private const val GIT_PASSWORD = "git-password"
private const val GIT_REPO = "git-repo"

interface FormState : RState {
    var appId: String
    var loading: Boolean

    var gitRepo: String
    var gitUsername: String
    var gitPassword: String
    var credentials: String

    var configs: MutableList<GitConfig>?
}

class AddAppForm : RComponent<RProps, FormState>() {
    fun onChange(event: Event, data: InputOnChangeData) {
        val target = (event.target as HTMLInputElement)
        setState {
            when (target.name) {
                GIT_USERNAME -> {
                    this.gitUsername = data.value
                }
                GIT_PASSWORD -> {
                    this.gitPassword = data.value
                }
                GIT_REPO -> {
                    this.gitRepo = data.value
                }
            }
        }
    }

    override fun RBuilder.render() {
        val errors = validate(state.gitUsername, state.gitPassword, state.gitRepo)
        Form {
            attrs.onSubmit = { event, data ->
                println(state)
                event.preventDefault()
            }
            attrs.loading = state.loading
            attrs.widths = "equal"
            Grid {
                attrs.padded = true
                appNameField()
                appIdInput(this@AddAppForm)
                addDirElement(state)
                gitConfig(state, errors, ::onChange)
                gradleConfigs(state, errors, ::onChange)
                submitButton()
            }
        }
    }

    private fun validate(username: String?, password: String?, repo: String?): MutableMap<String, Json?> {
        val error = mutableMapOf<String, Json?>()

        if (!username.isNullOrEmpty() && username.length < 3) {
            error[GIT_USERNAME] = "Enter a valid username".toUIError()
        }

        if (!password.isNullOrEmpty() && password.length < 3) {
            error[GIT_PASSWORD] = "Enter a valid password".toUIError()
        }

        if (!repo.isNullOrEmpty() && repo.length < 3) {
            error[GIT_REPO] = "Invalid repo url".toUIError()
        }

        return error
    }

    override fun componentDidMount() {
        setState {
            configs = mutableListOf(gitConfig { })
        }
    }
}

private fun RBuilder.gitRepoUrlField(
    state: FormState,
    errors: Map<String, Json?>,
    onChange: (Event, InputOnChangeData) -> Unit
): ReactElement {
    return Segment {
        FormField {
            FormInput {
                attrs.input = Input {
                    attrs["id"] = GIT_REPO
                    attrs["name"] = GIT_REPO
                    attrs["autoComplete"] = "on"
                    attrs["value"] = state.gitRepo
                    attrs.placeholder = "https://{domain}/{org_name}/{android_repo_name}.git"
                    attrs.type = InputType.url.realValue
                    attrs.onChange = onChange
                    attrs.labelPosition = "left"
                }
                attrs.label = "Git Repo url"
                attrs.error = errors[GIT_REPO]
            }
        }
    }
}

private fun RBuilder.appNameField(): ReactElement {
    return GridRow {
        GridColumn {
            FormGroup {
                FormField {
                    FormInput {
                        attrs.input = Input {
                            attrs["id"] = "input-app-name"
                            attrs.placeholder = "App name"
                            attrs.type = InputType.text.realValue
                        }
                        attrs.label = "App Name"
                    }
                }
            }
        }
    }
}

private fun RBuilder.appIdInput(component: RComponent<RProps, FormState>): ReactElement {
    return GridRow {
        GridColumn {
            FormGroup {
                FormInput {
                    attrs.input = Input {
                        attrs["id"] = "input-app-id"
                        attrs.placeholder = "Application id"
                        attrs.onChange = { event, _ ->
                            val target = event.target as HTMLInputElement
                            component.setState {
                                this.appId = target.value.ifNullOrEmpty {
                                    "{application_id}"
                                }
                            }
                        }
                        attrs.label = "App id"
                    }
                }
            }

        }
    }
}

private fun RBuilder.submitButton(): ReactElement {
    return GridRow {
        attrs.columns = 1
        GridColumn {
            FormButton {
                +"Submit"
            }
        }
    }
}

private fun RBuilder.addDirElement(state: FormState): ReactElement {
    return GridRow {
        this.attrs.verticalAlign = "middle"
        GridColumn {
            attrs.stretched = true
            FormGroup {
                FormField {
                    FormInput {
                        attrs.input = Input {
                            attrs.label = "app/git/${
                                state.appId.ifNullOrEmpty {
                                    "{application_id}"
                                }
                            }/application/"
                            attrs.labelPosition = "left"
                            attrs.placeholder = "Additional path(Optional)"
                            attrs.type = InputType.text.realValue
                            attrs.labelPosition = "left"
                        }
                        attrs.label = "Application directory"
                        attrs.id = "input-app-dir"
                    }
                }
            }
        }
    }
}

private fun RBuilder.gitConfig(
    state: FormState,
    errors: Map<String, Json?>,
    onChange: (Event, InputOnChangeData) -> Unit
): ReactElement {
    return GridRow {
        this.attrs.verticalAlign = "middle"
        GridColumn {
            attrs.stretched = true
            SegmentGroup {
                Segment {
                    h4 {
                        +"Git Config(Optional)"
                    }
                }
                SegmentGroup {
                    gitRepoUrlField(state, errors, onChange)
                    gitCredential(state, errors, onChange)
                    tagConfigSegment()
                    branchConfigSegment()
                }
            }
        }
    }
}

private fun RBuilder.tagConfigSegment(): ReactElement {
    return Segment {
        h4 {
            +"Tag Config"
        }
        FormInput {
            attrs.input = Input {
                attrs.labelPosition = "left"
                attrs.placeholder = "Tag Count"
                attrs.type = InputType.number.realValue
                attrs.defaultValue = "10"
                attrs.labelPosition = "left"
            }
            attrs.label = "Tag Count"
        }

        FormInput {
            attrs.input = Input {
                attrs.labelPosition = "left"
                attrs.placeholder = "eg. \\p{ASCII}*\$"
                attrs.type = InputType.text.realValue
                attrs.labelPosition = "left"
            }
            attrs.label = "Tag Regex"
        }
    }
}

private fun RBuilder.branchConfigSegment(): ReactElement {
    return Segment {
        h4 {
            +"Branch Config"
        }
        FormInput {
            attrs.input = Input {
                attrs.labelPosition = "left"
                attrs.placeholder = "Git Branch Count"
                attrs.type = InputType.number.realValue
                attrs.defaultValue = "10"
                attrs.labelPosition = "left"
            }
            attrs.label = "Branch Count"
        }

        FormInput {
            attrs.input = Input {
                attrs.labelPosition = "left"
                attrs.placeholder = "eg. \\p{ASCII}*\$"
                attrs.type = InputType.text.realValue
                attrs.labelPosition = "left"
            }
            attrs.label = "Branch Regex"
        }

        FormInput {
            attrs.input = Input {
                attrs.labelPosition = "left"
                attrs.placeholder = "master"
                attrs.type = InputType.text.realValue
                attrs.labelPosition = "left"
            }
            attrs.label = "Default Branch"
        }
    }
}

private fun RBuilder.gitCredential(
    state: FormState,
    errors: Map<String, Json?>,
    onChange: (Event, InputOnChangeData) -> Unit
): ReactElement {
    return Segment {
        h4 {
            +"Git repo credentials"
        }
        FormInput {
            attrs.input = Input {
                attrs["id"] = GIT_USERNAME
                attrs["name"] = GIT_USERNAME
                attrs["autoComplete"] = "on"
                attrs["value"] = state.gitUsername
                attrs.placeholder = "Username or token"
                attrs.type = InputType.text.realValue
                attrs.labelPosition = "left"
                attrs.onChange = onChange
            }
            attrs.label = "Username or token"
            attrs.error = errors[GIT_USERNAME]
        }

        FormInput {
            attrs.input = Input {
                attrs["autoComplete"] = "on"
                attrs["id"] = GIT_PASSWORD
                attrs["name"] = GIT_PASSWORD
                attrs["value"] = state.gitPassword
                attrs.type = InputType.password.realValue
                attrs.placeholder = "Password"
                attrs.labelPosition = "left"
                attrs.onChange = onChange
            }
            attrs.label = "Password"
            attrs.error = errors[GIT_PASSWORD]
        }
    }
}

private fun RBuilder.gradleConfigs(
    state: FormState,
    errors: Map<String, Json?>,
    onChange: (Event, InputOnChangeData) -> Unit
): ReactElement {
    return GridRow {
        this.attrs.verticalAlign = "middle"
        GridColumn {
            attrs.stretched = true
            SegmentGroup {
                Segment {
                    h4 {
                        +"Gradle Tasks"
                    }
                }

                state.configs?.forEach {
                    addGradleConfig(state, errors, onChange, it)
                }
            }
        }
    }
}

fun RBuilder.addGradleConfig(
    state: FormState,
    errors: Map<String, Json?>,
    onChange: (Event, InputOnChangeData) -> Unit,
    config: GitConfig
): ReactElement {
    return SegmentGroup {
        Segment {
            FormInput {
                attrs.input = Input {
                    attrs["id"] = "task_name_${config.uuid}"
                    attrs["name"] = "task_name_${config.uuid}"
                    attrs["autoComplete"] = "on"
                    attrs["value"] = config.id ?: ""
                    attrs.placeholder = "assemble debug"
                    attrs.type = InputType.text.realValue
                    attrs.labelPosition = "left"
                    attrs.onChange = onChange
                }
                attrs.label = "Task Name"
                attrs.error = errors["task_name_${config.uuid}"]
            }

            FormInput {
                attrs.input = Input {
                    attrs["autoComplete"] = "on"
                    attrs["id"] = config.uuid
                    attrs["name"] = config.uuid
                    attrs["value"] = state.gitPassword
                    attrs.type = InputType.text.realValue
                    attrs.placeholder = "Output dir"
                    attrs.labelPosition = "left"
                    attrs.onChange = onChange
                }
                attrs.label = "Output dir"
                attrs.error = errors[GIT_PASSWORD]
            }
        }
    }
}

fun RBuilder.addAppForm(handler: RProps.() -> Unit): ReactElement {
    return child(AddAppForm::class) {
        this.attrs(handler)
    }
}