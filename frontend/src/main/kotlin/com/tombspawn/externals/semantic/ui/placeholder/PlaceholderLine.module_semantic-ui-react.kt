@file:JsModule("semantic-ui-react/dist/commonjs/elements/Placeholder/PlaceholderLine")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.placeholder

import kotlin.js.*
import react.RClass
import react.RProps

external interface PlaceholderLineProps : StrictPlaceholderLineProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictPlaceholderLineProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var length: String? /* 'full' | 'very long' | 'long' | 'medium' | 'short' | 'very short' */
        get() = definedExternally
        set(value) = definedExternally
}

external interface PlaceholderLineComponent : RClass<PlaceholderLineProps>

@JsName("default")
external var PlaceholderLine: PlaceholderLineComponent = definedExternally