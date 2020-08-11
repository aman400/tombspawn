package com.tombspawn.component.skeleton

import com.tombspawn.externals.semantic.ui.button.Button
import com.tombspawn.externals.semantic.ui.form.FormRadio
import com.tombspawn.externals.semantic.ui.form.*
import react.*
import react.dom.dataList
import react.dom.div
import react.dom.option

class Skeleton : RComponent<RProps, RState>() {
    override fun RBuilder.render() {
        div {
            Form {
                this.attrs.widths = "equal"
                FormGroup {
                    FormField {
                        FormInput {
                            this.attrs.id = "input-app-name"
                            this.attrs.label = "Name"
                            this.attrs.defaultValue = "Dummy name"
                            this.attrs.list = "options"
                            this.attrs.type = "text"
                        }
                    }
                }

                FormField {
                    FormRadio {
                        this.attrs.label = "Text"
                    }
                }

                FormField {
                    this.attrs.control = Button
                    +"Submit"
                }
            }
        }
    }
}

fun RBuilder.skeleton(handler: RProps.() -> Unit): ReactElement {
    return child(Skeleton::class) {
        this.attrs(handler)
    }
}