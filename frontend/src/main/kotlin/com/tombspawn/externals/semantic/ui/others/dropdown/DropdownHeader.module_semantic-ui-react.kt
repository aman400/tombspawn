@file:JsModule("semantic-ui-react/dist/commonjs/modules/Dropdown/DropdownHeader")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.others.dropdown

import com.tombspawn.externals.semantic.ui.SemanticShorthandContent
import react.RClass
import react.RProps
import kotlin.js.*

external interface DropdownHeaderProps : StrictDropdownHeaderProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictDropdownHeaderProps: RProps {
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
    var icon: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.others.icon.IconProps? | com.tombspawn.externals.semantic.ui.SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.others.icon.IconProps>? */
        get() = definedExternally
        set(value) = definedExternally
}

external interface DropdownHeaderComponent : RClass<DropdownHeaderProps>

@JsName("default")
external var DropdownHeader: DropdownHeaderComponent = definedExternally