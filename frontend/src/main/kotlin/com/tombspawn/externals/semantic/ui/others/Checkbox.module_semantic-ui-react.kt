@file:JsModule("semantic-ui-react/dist/commonjs/modules/Checkbox/Checkbox")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.others

import kotlin.js.*
import org.w3c.dom.*
import org.w3c.dom.events.Event
import react.RClass
import react.RProps

external interface CheckboxProps : StrictCheckboxProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictCheckboxProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var checked: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var defaultChecked: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var defaultIndeterminate: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var fitted: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var id: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
    var indeterminate: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var label: dynamic /* React.ReactNode? | HtmlLabelProps? | SemanticShorthandItemFunc<HtmlLabelProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var name: String?
        get() = definedExternally
        set(value) = definedExternally
    var onChange: ((event: Event, data: CheckboxProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onClick: ((event: Event, data: CheckboxProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onMouseDown: ((event: Event, data: CheckboxProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onMouseUp: ((event: Event, data: CheckboxProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var radio: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var readOnly: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var slider: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var tabIndex: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
    var toggle: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var type: String? /* 'checkbox' | 'radio' */
        get() = definedExternally
        set(value) = definedExternally
    var value: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
}

external interface CheckboxComponent : RClass<CheckboxProps> {
}

@JsName("default")
external var Checkbox: CheckboxComponent = definedExternally