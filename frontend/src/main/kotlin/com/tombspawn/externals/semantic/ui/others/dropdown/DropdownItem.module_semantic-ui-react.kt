@file:JsModule("semantic-ui-react/dist/commonjs/modules/Dropdown/DropdownItem")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.others.dropdown

import com.tombspawn.externals.semantic.ui.SemanticShorthandContent
import kotlin.js.*
import org.w3c.dom.events.*
import react.RClass
import react.RProps

external interface DropdownItemProps : StrictDropdownItemProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictDropdownItemProps: RProps {
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
    var description: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.HtmlSpanProps? | com.tombspawn.externals.semantic.ui.SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.HtmlSpanProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var flag: dynamic /* React.ReactNode? | FlagProps? | com.tombspawn.externals.semantic.ui.SemanticShorthandItemFunc<FlagProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var icon: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.others.icon.IconProps? | com.tombspawn.externals.semantic.ui.SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.others.icon.IconProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var image: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.image.ImageProps? | com.tombspawn.externals.semantic.ui.SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.image.ImageProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var label: dynamic /* React.ReactNode? | LabelProps? | com.tombspawn.externals.semantic.ui.SemanticShorthandItemFunc<LabelProps>? */
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

external interface DropdownItemComponent : RClass<DropdownItemProps>

@JsName("default")
external var DropdownItem: DropdownItemComponent = definedExternally