@file:JsModule("semantic-ui-react/dist/commonjs/modules/Modal/Modal")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.modal

import com.tombspawn.externals.semantic.ui.others.portal.StrictPortalProps
import kotlin.js.*
import org.w3c.dom.*
import org.w3c.dom.events.Event
import react.RClass

external interface ModalProps : StrictModalProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictModalProps : StrictPortalProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var actions: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.modal.ModalActionsProps? | SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.modal.ModalActionsProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var basic: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var centered: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    override var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var closeIcon: Any?
        get() = definedExternally
        set(value) = definedExternally
    var closeOnDimmerClick: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    override var closeOnDocumentClick: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var content: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.modal.ModalContentProps? | SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.modal.ModalContentProps>? */
        get() = definedExternally
        set(value) = definedExternally
    override var defaultOpen: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var dimmer: dynamic /* Boolean? | String | String */
        get() = definedExternally
        set(value) = definedExternally
    override var eventPool: String?
        get() = definedExternally
        set(value) = definedExternally
    var header: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.modal.ModalHeaderProps? | SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.modal.ModalHeaderProps>? */
        get() = definedExternally
        set(value) = definedExternally
    override var mountNode: Any?
        get() = definedExternally
        set(value) = definedExternally
    var onActionClick: ((event: Event, data: ModalProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    override var open: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var size: String? /* 'mini' | 'tiny' | 'small' | 'large' | 'fullscreen' */
        get() = definedExternally
        set(value) = definedExternally
    var style: Any?
        get() = definedExternally
        set(value) = definedExternally
    override var trigger: Any?
        get() = definedExternally
        set(value) = definedExternally
}

external interface ModalComponent : RClass<ModalProps> {
    var Actions: Any
    var Content: Any
    var Description: Any
    var Header: Any
}

@JsName("default")
external var Modal: ModalComponent = definedExternally