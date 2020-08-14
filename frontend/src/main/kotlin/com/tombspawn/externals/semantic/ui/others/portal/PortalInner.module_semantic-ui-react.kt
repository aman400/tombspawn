@file:JsModule("semantic-ui-react/dist/commonjs/addons/Portal/PortalInner")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.others.portal

import react.RClass
import react.RProps
import kotlin.js.*

external interface PortalInnerProps : StrictPortalInnerProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictPortalInnerProps: RProps {
    var children: Any
    var innerRef: Any?
        get() = definedExternally
        set(value) = definedExternally
    var mountNode: Any?
        get() = definedExternally
        set(value) = definedExternally
    var onMount: ((nothing: Nothing?, data: PortalInnerProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onUnmount: ((nothing: Nothing?, data: PortalInnerProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
}

external interface PortalInnerComponent : RClass<PortalInnerProps>

@JsName("default")
external var PortalInner: PortalInnerComponent = definedExternally