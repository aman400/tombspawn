@file:JsModule("semantic-ui-react/dist/commonjs/collections/Menu/MenuItem")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.menu

import com.tombspawn.externals.semantic.ui.SemanticShorthandContent
import org.w3c.dom.events.MouseEvent
import kotlin.js.*
import react.RClass
import react.RProps

external interface MenuItemProps : StrictMenuItemProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictMenuItemProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var active: Boolean?
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
    var content: SemanticShorthandContent?
        get() = definedExternally
        set(value) = definedExternally
    var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var fitted: dynamic /* Boolean? | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var header: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var icon: dynamic /* Boolean? | React.ReactNode? | com.tombspawn.externals.semantic.ui.others.icon.IconProps? | com.tombspawn.externals.semantic.ui.SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.others.icon.IconProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var index: Number?
        get() = definedExternally
        set(value) = definedExternally
    var link: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var name: String?
        get() = definedExternally
        set(value) = definedExternally
    var onClick: ((event: (event: MouseEvent) -> Unit, data: MenuItemProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var position: String? /* 'left' | 'right' */
        get() = definedExternally
        set(value) = definedExternally
}

external interface MenuItemComponent : RClass<MenuItemProps> {
}

@JsName("default")
external var MenuItem: MenuItemComponent = definedExternally