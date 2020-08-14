@file:JsModule("semantic-ui-react/dist/commonjs/collections/Message/Message")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.message

import com.tombspawn.externals.semantic.ui.SemanticShorthandCollection
import com.tombspawn.externals.semantic.ui.SemanticShorthandContent
import kotlin.js.*
import org.w3c.dom.events.*
import react.RClass
import react.RProps

external interface MessageProps : StrictMessageProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictMessageProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var attached: dynamic /* Boolean? | String | String */
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
    var compact: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var content: SemanticShorthandContent?
        get() = definedExternally
        set(value) = definedExternally
    var error: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var floating: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var header: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.message.MessageHeaderProps? | com.tombspawn.externals.semantic.ui.SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.message.MessageHeaderProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var hidden: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var icon: dynamic /* Any? | Boolean? */
        get() = definedExternally
        set(value) = definedExternally
    var info: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var list: SemanticShorthandCollection<MessageItemProps>?
        get() = definedExternally
        set(value) = definedExternally
    var negative: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var onDismiss: ((event: Event, data: MessageProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var positive: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var size: String? /* 'mini' | 'tiny' | 'small' | 'large' | 'big' | 'huge' | 'massive' */
        get() = definedExternally
        set(value) = definedExternally
    var success: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var visible: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var warning: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface MessageComponent : RClass<MessageProps> {
    var Content: Any
    var Header: Any
    var List: Any
    var Item: Any
}

@JsName("default")
external var Message: MessageComponent = definedExternally