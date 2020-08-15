@file:JsModule("semantic-ui-react/dist/commonjs/modules/Dropdown/DropdownMenu")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.others.dropdown

import com.tombspawn.externals.semantic.ui.SemanticShorthandContent
import react.RClass
import react.RProps
import kotlin.js.*

external interface DropdownMenuProps : StrictDropdownMenuProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictDropdownMenuProps: RProps {
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
    var direction: String? /* 'left' | 'right' */
        get() = definedExternally
        set(value) = definedExternally
    var open: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var scrolling: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface DropdownMenuComponent : RClass<DropdownMenuProps>

@JsName("default")
external var DropdownMenu: DropdownMenuComponent = definedExternally