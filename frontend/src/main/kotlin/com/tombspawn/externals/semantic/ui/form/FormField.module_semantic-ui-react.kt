@file:JsModule("semantic-ui-react/dist/commonjs/collections/Form/FormField")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.form

import com.tombspawn.externals.semantic.ui.HtmlLabelProps
import com.tombspawn.externals.semantic.ui.SemanticShorthandContent
import kotlin.js.*
import react.RClass
import react.RProps
import react.ReactElement

external interface FormFieldProps : StrictFormFieldProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictFormFieldProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var content: SemanticShorthandContent?
        get() = definedExternally
        set(value) = definedExternally
    var control: Any?
        get() = definedExternally
        set(value) = definedExternally
    var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var error: dynamic /* Boolean? | React.ReactNode? | LabelProps? | SemanticShorthandItemFunc<LabelProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var id: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
    var inline: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var label: dynamic /* React.ReactNode? | HtmlLabelProps? | SemanticShorthandItemFunc<HtmlLabelProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var required: Any?
        get() = definedExternally
        set(value) = definedExternally
    var type: String?
        get() = definedExternally
        set(value) = definedExternally
    var width: dynamic /* Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
}

abstract external class FormFieldComponent : RClass<FormFieldProps> {
}

@JsName("default")
external var FormField: FormFieldComponent = definedExternally