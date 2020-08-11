@file:JsModule("semantic-ui-react/dist/commonjs/collections/Form/Form")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.form

import kotlin.js.*
import org.w3c.dom.events.*
import react.RClass
import react.RProps

external interface FormProps : StrictFormProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictFormProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var action: String?
        get() = definedExternally
        set(value) = definedExternally
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var error: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var inverted: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var loading: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var onSubmit: ((event: Event, data: FormProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var reply: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var size: String?
        get() = definedExternally
        set(value) = definedExternally
    var success: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var unstackable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var warning: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var widths: String? /* 'equal' */
        get() = definedExternally
        set(value) = definedExternally
}

abstract external interface FormComponent : RClass<FormProps> {
    var Field: Any
    var Button: Any
    var Checkbox: Any
    var Dropdown: Any
    var Group: Any
    var Input: Any
    var Radio: Any
    var Select: Any
    var TextArea: Any
}

@JsName("default")
external var Form: FormComponent = definedExternally