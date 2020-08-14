@file:JsModule("semantic-ui-react/dist/commonjs/collections/Message/MessageHeader")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.message

import com.tombspawn.externals.semantic.ui.SemanticShorthandContent
import kotlin.js.*
import react.RClass
import react.RProps

external interface MessageHeaderProps : StrictMessageHeaderProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictMessageHeaderProps: RProps {
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

external interface MessageHeaderComponent : RClass<MessageHeaderProps>

@JsName("default")
external var MessageHeader: MessageHeaderComponent = definedExternally