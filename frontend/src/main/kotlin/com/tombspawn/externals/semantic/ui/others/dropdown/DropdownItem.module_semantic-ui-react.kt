@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.others.dropdown

import SemanticShorthandContent
import kotlin.js.*
import org.w3c.dom.events.*

external interface DropdownItemProps : StrictDropdownItemProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictDropdownItemProps {
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
    var content: SemanticShorthandContent?
        get() = definedExternally
        set(value) = definedExternally
    var description: dynamic /* React.ReactNode? | HtmlSpanProps? | SemanticShorthandItemFunc<HtmlSpanProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var flag: dynamic /* React.ReactNode? | FlagProps? | SemanticShorthandItemFunc<FlagProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var icon: dynamic /* React.ReactNode? | IconProps? | SemanticShorthandItemFunc<IconProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var image: dynamic /* React.ReactNode? | ImageProps? | SemanticShorthandItemFunc<ImageProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var label: dynamic /* React.ReactNode? | LabelProps? | SemanticShorthandItemFunc<LabelProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var onClick: ((event: Event, data: DropdownItemProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var selected: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var text: SemanticShorthandContent?
        get() = definedExternally
        set(value) = definedExternally
    var value: dynamic /* Boolean? | Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
}