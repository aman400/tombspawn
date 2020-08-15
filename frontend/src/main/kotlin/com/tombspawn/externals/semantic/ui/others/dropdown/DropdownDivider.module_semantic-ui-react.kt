@file:JsModule("semantic-ui-react/dist/commonjs/modules/Dropdown/DropdownDivider")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.others.dropdown

import react.RClass
import react.RProps
import kotlin.js.*

external interface DropdownDividerProps : StrictDropdownDividerProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictDropdownDividerProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface DropdownDividerComponent : RClass<DropdownDividerProps>

@JsName("default")
external var DropdownDivider: DropdownDividerComponent = definedExternally