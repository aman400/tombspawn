@file:JsModule("semantic-ui-react/dist/commonjs/addons/Radio/Radio")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.others

import react.RClass
import kotlin.js.*

external interface RadioProps : StrictRadioProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictRadioProps : StrictCheckboxProps {
    override var slider: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    override var toggle: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    override var type: String? /* 'checkbox' | 'radio' */
        get() = definedExternally
        set(value) = definedExternally
}

external interface RadioComponent : RClass<RadioProps> {
}

@JsName("default")
external var Radio: RadioComponent = definedExternally