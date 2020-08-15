@file:JsModule("semantic-ui-react/dist/commonjs/modules/Modal/ModalActions")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.modal

import com.tombspawn.externals.semantic.ui.SemanticShorthandCollection
import com.tombspawn.externals.semantic.ui.SemanticShorthandContent
import com.tombspawn.externals.semantic.ui.button.ButtonProps
import com.tombspawn.externals.semantic.ui.others.CommentTextProps
import kotlin.js.*
import org.w3c.dom.events.*
import react.RClass
import react.RProps

external interface ModalActionsProps : StrictModalActionsProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictModalActionsProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var actions: SemanticShorthandCollection<ButtonProps>?
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
    var onActionClick: ((event: Event, data: ButtonProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
}

external interface ModalActionsComponent : RClass<ModalActionsProps>

@JsName("default")
external var ModalActions: ModalActionsComponent = definedExternally