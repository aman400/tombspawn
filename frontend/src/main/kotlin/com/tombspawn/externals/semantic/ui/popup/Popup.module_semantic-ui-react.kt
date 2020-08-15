@file:JsModule("semantic-ui-react/dist/commonjs/modules/Popup/Popup")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.popup

import com.tombspawn.externals.semantic.ui.others.portal.StrictPortalProps
import com.tombspawn.externals.semantic.utils.Record
import react.RClass
import kotlin.js.*

external interface PopupProps : StrictPopupProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictPopupProps : StrictPortalProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var basic: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    override var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var content: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.popup.PopupContentProps? | com.tombspawn.externals.semantic.ui.SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.popup.PopupContentProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var context: dynamic /* Document? | Window? | HTMLElement? | React.RefObject<HTMLElement>? */
        get() = definedExternally
        set(value) = definedExternally
    var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var eventsEnabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var flowing: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var header: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.popup.PopupHeaderProps? | com.tombspawn.externals.semantic.ui.SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.popup.PopupHeaderProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var hideOnScroll: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var hoverable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var inverted: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var offset: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
    var on: dynamic /* String | String | String | Array<String /* 'focus' */>? */
        get() = definedExternally
        set(value) = definedExternally
    var pinned: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var position: String? /* 'top left' | 'top right' | 'bottom right' | 'bottom left' | 'right center' | 'left center' | 'top center' | 'bottom center' */
        get() = definedExternally
        set(value) = definedExternally
    var positionFixed: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var popperModifiers: Record<String, Any>?
        get() = definedExternally
        set(value) = definedExternally
    var popperDependencies: Array<Any>?
        get() = definedExternally
        set(value) = definedExternally
    var size: String? /* 'mini' | 'tiny' | 'small' | 'large' | 'huge' */
        get() = definedExternally
        set(value) = definedExternally
    var style: Any?
        get() = definedExternally
        set(value) = definedExternally
    override var trigger: Any?
        get() = definedExternally
        set(value) = definedExternally
    var wide: dynamic /* Boolean? | String */
        get() = definedExternally
        set(value) = definedExternally
}

external interface PopupComponent : RClass<PopupProps> {
//    var Content: PopupContentComponent
//    var com.tombspawn.externals.semantic.ui.others.header.getHeader: PopupHeaderComponent
}

@JsName("default")
external var Popup: PopupComponent = definedExternally