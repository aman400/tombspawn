@file:JsModule("semantic-ui-react/dist/commonjs/views/Item/ItemContent")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.views.item

import com.tombspawn.externals.semantic.ui.SemanticShorthandContent
import react.RClass
import react.RProps

external interface ItemContentProps : StrictItemContentProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictItemContentProps: RProps {
    var `as`: Any?
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
    var description: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.views.item.ItemDescriptionProps? | com.tombspawn.externals.semantic.ui.SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.views.item.ItemDescriptionProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var extra: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.views.item.ItemExtraProps? | com.tombspawn.externals.semantic.ui.SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.views.item.ItemExtraProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var header: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.views.item.ItemHeaderProps? | com.tombspawn.externals.semantic.ui.SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.views.item.ItemHeaderProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var meta: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.views.item.ItemMetaProps? | com.tombspawn.externals.semantic.ui.SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.views.item.ItemMetaProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var verticalAlign: String? /* 'top' | 'middle' | 'bottom' */
        get() = definedExternally
        set(value) = definedExternally
}

external interface ItemContentComponent : RClass<ItemContentProps> {
}

@JsName("default")
external var ItemContent: ItemContentComponent = definedExternally