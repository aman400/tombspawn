package com.tombspawn.component.skeleton

import com.tombspawn.externals.semantic.ui.form.FormRadio
import com.tombspawn.externals.semantic.ui.form.*
import com.tombspawn.externals.semantic.ui.others.Input
import react.*
import react.dom.div

class Skeleton : RComponent<RProps, RState>() {
    override fun RBuilder.render() {
        div {
            Form {
                FormGroup {
                    FormField {
                        FormInput {
                            this.attrs.id = "form-input-control-last-name"
                            this.attrs.control = Input
                            this.attrs.label = "Name"
                        }
                    }

                    FormField {
                        FormRadio {
                            this.attrs.label = "Text"
                        }
                    }
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