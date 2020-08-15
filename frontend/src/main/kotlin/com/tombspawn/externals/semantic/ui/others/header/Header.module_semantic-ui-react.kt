@file:JsModule("semantic-ui-react/dist/commonjs/elements/Header/Header")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.others.header

import kotlin.js.*
import react.RClass
import react.RProps

external interface HeaderProps : StrictHeaderProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictHeaderProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var attached: dynamic /* Boolean? | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var block: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var color: String? /* 'red' | 'orange' | 'yellow' | 'olive' | 'green' | 'teal' | 'blue' | 'violet' | 'purple' | 'pink' | 'brown' | 'grey' | 'black' */
        get() = definedExternally
        set(value) = definedExternally
    var content: Any?
        get() = definedExternally
        set(value) = definedExternally
    var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var dividing: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var floated: String? /* 'left' | 'right' */
        get() = definedExternally
        set(value) = definedExternally
    var icon: Any?
        get() = definedExternally
        set(value) = definedExternally
    var image: Any?
        get() = definedExternally
        set(value) = definedExternally
    var inverted: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var size: String? /* 'tiny' | 'small' | 'medium' | 'large' | 'huge' */
        get() = definedExternally
        set(value) = definedExternally
    var sub: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var subheader: Any?
        get() = definedExternally
        set(value) = definedExternally
    var textAlign: String? /* 'left' | 'center' | 'right' | 'justified' */
        get() = definedExternally
        set(value) = definedExternally
}

external interface HeaderComponent : RClass<HeaderProps> {
    var Content: Any
    var Subheader: Any
}

@JsName("default")
external var Header: HeaderComponent = definedExternally