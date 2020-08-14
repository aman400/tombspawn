@file:JsModule("semantic-ui-react/dist/commonjs/collections/Message/MessageItem")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.message

import com.tombspawn.externals.semantic.ui.SemanticShorthandContent
import kotlin.js.*
import react.RClass
import react.RProps

external interface MessageItemProps : StrictMessageItemProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictMessageItemProps: RProps {
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
}

external interface MessageItemComponent : RClass<MessageItemProps>

@JsName("default")
external var MessageItem: MessageItemComponent = definedExternally