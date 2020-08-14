@file:JsModule("semantic-ui-react/dist/commonjs/elements/Placeholder/Placeholder")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.placeholder

import com.tombspawn.externals.semantic.ui.SemanticShorthandContent
import kotlin.js.*
import react.RClass
import react.RProps

external interface PlaceholderProps : StrictPlaceholderProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictPlaceholderProps: RProps {
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
    var fluid: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var inverted: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface PlaceholderComponent : RClass<PlaceholderProps> {
    var Header: Any
    var Line: Any
    var Image: Any
    var Paragraph: Any
}

@JsName("default")
external var Placeholder: PlaceholderComponent = definedExternally