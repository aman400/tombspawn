@file:JsModule("semantic-ui-react/dist/commonjs/elements/Placeholder/PlaceholderImage")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.placeholder

import react.RClass
import react.RProps
import kotlin.js.*

external interface PlaceholderImageProps : StrictPlaceholderImageProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictPlaceholderImageProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var square: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var rectangular: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface PlaceholderImageComponent : RClass<PlaceholderImageProps>

@JsName("default")
external var PlaceholderImage: PlaceholderImageComponent = definedExternally