@file:JsModule("semantic-ui-react/dist/commonjs/collections/Form/FormButton")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.form

import SemanticShorthandContent
import com.tombspawn.externals.semantic.ui.button.StrictButtonProps
import react.RClass
import kotlin.js.*

external interface FormButtonProps : StrictFormButtonProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictFormButtonProps : StrictFormFieldProps, StrictButtonProps {
    override var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    override var control: Any?
        get() = definedExternally
        set(value) = definedExternally
    override var label: dynamic /* React.ReactNode? | LabelProps? | SemanticShorthandItemFunc<LabelProps>? */
        get() = definedExternally
        set(value) = definedExternally
    override var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    override var className: String?
        get() = definedExternally
        set(value) = definedExternally
    override var content: SemanticShorthandContent?
        get() = definedExternally
        set(value) = definedExternally
    override var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

abstract external class FormButtonComponent : RClass<FormButtonProps> {
}

@JsName("default")
external var FormButton: FormRadioComponent = definedExternally