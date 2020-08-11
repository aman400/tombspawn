@file:JsModule("semantic-ui-react/dist/commonjs/modules/Dropdown/DropdownSearchInput")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.others.dropdown

import react.RClass
import react.RProps
import kotlin.js.*

external interface DropdownSearchInputProps : StrictDropdownSearchInputProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictDropdownSearchInputProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var autoComplete: String?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var tabIndex: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
    var type: String?
        get() = definedExternally
        set(value) = definedExternally
    var value: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
}

external interface DropdownSearchInputComponent : RClass<DropdownSearchInputProps> {
}

@JsName("default")
external var DropdownSearchInput: DropdownSearchInputComponent = definedExternally