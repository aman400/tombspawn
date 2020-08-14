@file:JsModule("semantic-ui-react/dist/commonjs/addons/Portal/Portal")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.others.portal

import org.w3c.dom.events.Event
import react.RClass
import react.RProps

external interface PortalProps : StrictPortalProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictPortalProps: RProps {
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var closeOnDocumentClick: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var closeOnEscape: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var closeOnPortalMouseLeave: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var closeOnTriggerBlur: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var closeOnTriggerClick: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var closeOnTriggerMouseLeave: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var defaultOpen: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var eventPool: String?
        get() = definedExternally
        set(value) = definedExternally
    var mountNode: Any?
        get() = definedExternally
        set(value) = definedExternally
    var mouseEnterDelay: Number?
        get() = definedExternally
        set(value) = definedExternally
    var mouseLeaveDelay: Number?
        get() = definedExternally
        set(value) = definedExternally
    var onClose: ((event: Event, data: PortalProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onMount: ((nothing: Nothing?, data: PortalProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onOpen: ((event: Event, data: PortalProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onUnmount: ((nothing: Nothing?, data: PortalProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var open: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var openOnTriggerClick: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var openOnTriggerFocus: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var openOnTriggerMouseEnter: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var trigger: Any?
        get() = definedExternally
        set(value) = definedExternally
    var triggerRef: Any?
        get() = definedExternally
        set(value) = definedExternally
}


external interface PortalComponent : RClass<PortalProps> {
    var Inner: Any
}

@JsName("default")
external var Portal: PortalComponent = definedExternally