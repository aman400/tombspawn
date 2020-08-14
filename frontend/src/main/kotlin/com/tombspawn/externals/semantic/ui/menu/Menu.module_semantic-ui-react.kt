@file:JsModule("semantic-ui-react/dist/commonjs/collections/Menu/Menu")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.menu

import com.tombspawn.externals.semantic.ui.SemanticShorthandCollection
import kotlin.js.*
import react.RClass
import react.RProps

external interface MenuProps : StrictMenuProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictMenuProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var activeIndex: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
    var attached: dynamic /* Boolean? | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var borderless: Boolean?
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
    var defaultActiveIndex: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
    var fixed: String? /* 'left' | 'right' | 'bottom' | 'top' */
        get() = definedExternally
        set(value) = definedExternally
    var floated: dynamic /* Boolean? | String */
        get() = definedExternally
        set(value) = definedExternally
    var fluid: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var icon: dynamic /* Boolean? | String */
        get() = definedExternally
        set(value) = definedExternally
    var inverted: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var items: SemanticShorthandCollection<MenuItemProps>?
        get() = definedExternally
        set(value) = definedExternally
//    var onItemClick: ((event: (event: MouseEvent) -> Unit, data: ButtonProps) -> Unit)?
//        get() = definedExternally
//        set(value) = definedExternally
    var pagination: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var pointing: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var secondary: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var size: String? /* 'mini' | 'tiny' | 'small' | 'large' | 'huge' | 'massive' */
        get() = definedExternally
        set(value) = definedExternally
    var stackable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var tabular: dynamic /* Boolean? | String */
        get() = definedExternally
        set(value) = definedExternally
    var text: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var vertical: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var widths: dynamic /* Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
}

external interface MenuComponent : RClass<MenuProps> {
//    var Header: Any
//    var Item: Any
//    var menuItems: List<MenuItemComponent>
}

@JsName("default")
external var Menu: MenuComponent = definedExternally