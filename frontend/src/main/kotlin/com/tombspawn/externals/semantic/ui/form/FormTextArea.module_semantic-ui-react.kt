@file:JsModule("semantic-ui-react/dist/commonjs/collections/Form/FormTextArea")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.form

import com.tombspawn.externals.semantic.ui.others.TextAreaProps
import kotlin.js.*
import react.RClass

external interface FormTextAreaProps : StrictFormTextAreaProps

external interface StrictFormTextAreaProps : StrictFormFieldProps, TextAreaProps {
    override var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    override var control: Any?
        get() = definedExternally
        set(value) = definedExternally
}

abstract external class FormTextAreaComponent : RClass<FormTextAreaProps> {
    override var displayName: String? = definedExternally
    var focus: () -> Unit
}

@JsName("default")
external var FormTextArea: FormTextAreaComponent = definedExternally
