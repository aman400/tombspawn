@file:JsModule("semantic-ui-react/dist/commonjs/elements/Image/Image")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.image

import SemanticShorthandContent
import react.RClass
import react.RProps
import kotlin.js.*

external interface ImageProps : StrictImageProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictImageProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var avatar: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var bordered: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var centered: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var circular: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var content: SemanticShorthandContent?
        get() = definedExternally
        set(value) = definedExternally
    var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var dimmer: dynamic /* React.ReactNode? | DimmerProps? | SemanticShorthandItemFunc<DimmerProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var floated: String? /* 'left' | 'right' */
        get() = definedExternally
        set(value) = definedExternally
    var fluid: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var hidden: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var href: String?
        get() = definedExternally
        set(value) = definedExternally
    var inline: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var label: dynamic /* React.ReactNode? | LabelProps? | SemanticShorthandItemFunc<LabelProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var rounded: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var size: String? /* 'mini' | 'tiny' | 'small' | 'medium' | 'large' | 'big' | 'huge' | 'massive' */
        get() = definedExternally
        set(value) = definedExternally
    var spaced: dynamic /* Boolean? | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var ui: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var verticalAlign: String? /* 'top' | 'middle' | 'bottom' */
        get() = definedExternally
        set(value) = definedExternally
    var wrapped: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface ImageComponent : RClass<ImageProps> {
    var Group: Any
}

@JsName("default")
external var Image: ImageComponent = definedExternally