@file:JsModule("semantic-ui-react/dist/commonjs/addons/Select/Select")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.others

import com.tombspawn.externals.semantic.ui.others.dropdown.StrictDropdownProps
import react.RClass
import kotlin.js.*

external interface SelectProps : StrictSelectProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictSelectProps : StrictDropdownProps

external interface SelectComponent : RClass<SelectProps> {
    var Divider: Any
    var Header: Any
    var Item: Any
    var Menu: Any
}

@JsName("default")
external var Select: SelectComponent = definedExternally