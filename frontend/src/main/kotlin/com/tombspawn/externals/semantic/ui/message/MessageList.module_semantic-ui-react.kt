@file:JsModule("semantic-ui-react/dist/commonjs/collections/Message/MessageList")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.message

import com.tombspawn.externals.semantic.ui.SemanticShorthandCollection
import react.RClass
import react.RProps
import kotlin.js.*

external interface MessageListProps : StrictMessageListProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictMessageListProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var items: SemanticShorthandCollection<MessageItemProps>?
        get() = definedExternally
        set(value) = definedExternally
}

external interface MessageListComponent : RClass<MessageListProps>

@JsName("default")
external var MessageList: MessageListComponent = definedExternally