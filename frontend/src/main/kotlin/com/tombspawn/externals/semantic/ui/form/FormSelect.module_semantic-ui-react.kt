@file:JsModule("semantic-ui-react/dist/commonjs/collections/Form/FormSelect")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.form

import com.tombspawn.externals.semantic.ui.others.StrictSelectProps
import react.RClass
import kotlin.js.*

external interface FormSelectProps : StrictFormSelectProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictFormSelectProps : StrictFormFieldProps, StrictSelectProps {
    override var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    override var control: Any?
        get() = definedExternally
        set(value) = definedExternally

    override var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    override var className: String?
        get() = definedExternally
        set(value) = definedExternally
    override var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    override var error: dynamic
        get() = definedExternally
        set(value) = definedExternally
    override var inline: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

abstract external class FormSelectComponent : RClass<FormSelectProps> {
}

@JsName("default")
external var FormSelect: FormSelectComponent = definedExternally