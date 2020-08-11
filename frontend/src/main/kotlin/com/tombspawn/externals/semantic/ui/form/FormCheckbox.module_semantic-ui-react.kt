@file:JsModule("semantic-ui-react/dist/commonjs/collections/Form/FormCheckbox")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.form

import com.tombspawn.externals.semantic.ui.others.StrictCheckboxProps
import react.RClass
import kotlin.js.*

external interface FormCheckboxProps : StrictFormCheckboxProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictFormCheckboxProps : StrictFormFieldProps, StrictCheckboxProps {
    override var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    override var control: Any?
        get() = definedExternally
        set(value) = definedExternally
    override var type: String? /* 'checkbox' | 'radio' */
        get() = definedExternally
        set(value) = definedExternally
    override var className: String?
        get() = definedExternally
        set(value) = definedExternally
    override var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    override var id: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
    override var label: dynamic /* React.ReactNode? | HtmlLabelProps? | SemanticShorthandItemFunc<HtmlLabelProps>? */
        get() = definedExternally
        set(value) = definedExternally
}

abstract external class FormCheckboxComponent : RClass<FormCheckboxProps> {
}

@JsName("default")
external var FormCheckbox: FormRadioComponent = definedExternally