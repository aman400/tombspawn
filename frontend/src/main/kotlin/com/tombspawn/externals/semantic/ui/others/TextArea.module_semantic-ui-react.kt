@file:JsModule("semantic-ui-react/dist/commonjs/addons/TextArea/TextArea")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.others

import org.w3c.dom.events.Event
import react.RClass
import react.RProps

external interface TextAreaProps : StrictTextAreaProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictTextAreaProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var onChange: ((event: Event, data: TextAreaProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onInput: ((event: Event, data: TextAreaProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var rows: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
    var value: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
}

abstract external class TextAreaComponent : RClass<TextAreaProps> {
    open var focus: () -> Unit
}

@JsName("default")
external var TextArea: TextAreaComponent = definedExternally