@file:JsModule("semantic-ui-react/dist/commonjs/elements/Placeholder/PlaceholderHeader")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.placeholder

import com.tombspawn.externals.semantic.ui.SemanticShorthandContent
import react.RClass
import react.RProps
import kotlin.js.*

external interface PlaceholderHeaderProps : StrictPlaceholderHeaderProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictPlaceholderHeaderProps: RProps {
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
    var image: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface PlaceholderHeaderComponent : RClass<PlaceholderHeaderProps>

@JsName("default")
external var PlaceholderHeader: PlaceholderHeaderComponent = definedExternally