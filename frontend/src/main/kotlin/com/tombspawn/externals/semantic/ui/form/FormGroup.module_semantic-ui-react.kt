@file:JsModule("semantic-ui-react/dist/commonjs/collections/Form/FormGroup")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.form

import react.RClass
import react.RProps
import kotlin.js.*

external interface FormGroupProps : StrictFormGroupProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictFormGroupProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var grouped: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var inline: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var unstackable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var widths: dynamic /* Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
}

abstract external class FormGroupComponent : RClass<FormGroupProps> {
}

@JsName("default")
external var FormGroup: FormGroupComponent = definedExternally