@file:JsModule("semantic-ui-react/dist/commonjs/collections/Form/FormRadio")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.form

import com.tombspawn.externals.semantic.ui.others.StrictRadioProps
import kotlin.js.*
import react.RClass

external interface FormRadioProps : StrictFormRadioProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictFormRadioProps : StrictFormFieldProps, StrictRadioProps {
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
    override var id: dynamic
        get() = definedExternally
        set(value) = definedExternally
    override var label: dynamic
        get() = definedExternally
        set(value) = definedExternally
}

abstract external class FormRadioComponent : RClass<FormRadioProps> {
}

@JsName("default")
external var FormRadio: FormRadioComponent = definedExternally