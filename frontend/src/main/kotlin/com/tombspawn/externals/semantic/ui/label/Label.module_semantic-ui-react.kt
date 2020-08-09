@file:JsModule("semantic-ui-react/dist/commonjs/elements/Label/Label")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.label

import SemanticShorthandContent
import com.tombspawn.externals.semantic.ui.menu.MenuItemProps
import kotlin.js.*
import org.w3c.dom.events.*
import react.RClass
import react.RProps

external interface LabelProps : StrictLabelProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictLabelProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var active: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var attached: String? /* 'top' | 'bottom' | 'top right' | 'top left' | 'bottom left' | 'bottom right' */
        get() = definedExternally
        set(value) = definedExternally
    var basic: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var circular: Boolean?
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
    var corner: dynamic /* Boolean? | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var detail: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.label.LabelDetailProps? | SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.label.LabelDetailProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var empty: Any?
        get() = definedExternally
        set(value) = definedExternally
    var floating: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var horizontal: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var icon: dynamic /* React.ReactNode? | IconProps? | SemanticShorthandItemFunc<IconProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var image: Any?
        get() = definedExternally
        set(value) = definedExternally
    var onClick: ((event: (event: MouseEvent) -> Unit, data: LabelProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onRemove: ((event: (event: MouseEvent) -> Unit, data: LabelProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var pointing: dynamic /* Boolean? | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var prompt: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var removeIcon: dynamic /* React.ReactNode? | IconProps? | SemanticShorthandItemFunc<IconProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var ribbon: dynamic /* Boolean? | String */
        get() = definedExternally
        set(value) = definedExternally
    var size: String? /* 'mini' | 'tiny' | 'small' | 'medium' | 'large' | 'big' | 'huge' | 'massive' */
        get() = definedExternally
        set(value) = definedExternally
    var tag: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}


external interface LabelComponent : RClass<LabelProps> {
    var Detail: Any
    var Group: Any
}

@JsName("default")
external var Label: LabelComponent = definedExternally