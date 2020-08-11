@file:JsModule("semantic-ui-react/dist/commonjs/elements/Button/ButtonOr")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.button

import react.RClass
import react.RProps
import kotlin.js.*

external interface ButtonOrProps : StrictButtonOrProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictButtonOrProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var text: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
}


abstract external class ButtonOrComponent : RClass<ButtonOrProps>

@JsName("default")
external var ButtonOr: ButtonOrComponent = definedExternally