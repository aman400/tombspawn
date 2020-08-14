@file:JsModule("semantic-ui-react/dist/commonjs/elements/Input/Input")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.others

import kotlin.js.*
import org.w3c.dom.events.*
import react.RClass
import react.RProps

external interface InputProps : StrictInputProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictInputProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var action: dynamic /* Any? | Boolean? */
        get() = definedExternally
        set(value) = definedExternally
    var actionPosition: String? /* 'left' */
        get() = definedExternally
        set(value) = definedExternally
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var error: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var fluid: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var focus: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var icon: dynamic /* Any? | React.ReactNode? | InputProps? | SemanticShorthandItemFunc<InputProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var iconPosition: String? /* 'left' */
        get() = definedExternally
        set(value) = definedExternally
    var input: dynamic /* React.ReactNode? | HtmlInputProps? | SemanticShorthandItemFunc<HtmlInputProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var inverted: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var label: dynamic /* React.ReactNode? | LabelProps? | SemanticShorthandItemFunc<LabelProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var labelPosition: String? /* 'left' | 'right' | 'left corner' | 'right corner' */
        get() = definedExternally
        set(value) = definedExternally
    var loading: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var onChange: ((event: Event, data: InputOnChangeData) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var size: String? /* 'mini' | 'small' | 'large' | 'big' | 'huge' | 'massive' */
        get() = definedExternally
        set(value) = definedExternally
    var tabIndex: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
    var transparent: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var type: String?
        get() = definedExternally
        set(value) = definedExternally
    var defaultValue: String?
        get() = definedExternally
        set(value) = definedExternally
    var list: String?
        get() = definedExternally
        set(value) = definedExternally
    var placeholder: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface InputOnChangeData : InputProps {
    var value: String
}

external interface InputComponent : RClass<InputProps> {
    var focus: () -> Unit
    var select: () -> Unit
}

@JsName("default")
external var Input: InputComponent = definedExternally