@file:JsModule("semantic-ui-react/dist/commonjs/collections/Menu/MenuMenu")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.menu

import SemanticShorthandContent
import react.RClass
import react.RProps
import kotlin.js.*

external interface MenuMenuProps : StrictMenuMenuProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictMenuMenuProps: RProps {
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
    var position: String? /* 'left' | 'right' */
        get() = definedExternally
        set(value) = definedExternally
}


external interface MenuMenuComponent : RClass<MenuMenuProps> {
}

@JsName("default")
external var MenuMenu: MenuMenuComponent = definedExternally