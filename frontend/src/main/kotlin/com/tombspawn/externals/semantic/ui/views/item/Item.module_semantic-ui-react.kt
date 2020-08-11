@file:JsModule("semantic-ui-react/dist/commonjs/views/Item/Item")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.views.item

import SemanticShorthandContent
import com.tombspawn.externals.semantic.ui.table.TableComponent
import react.RClass
import react.RProps
import kotlin.js.*

external interface ItemProps : StrictItemProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictItemProps: RProps {
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
    var description: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.views.item.ItemDescriptionProps? | SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.views.item.ItemDescriptionProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var extra: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.views.item.ItemExtraProps? | SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.views.item.ItemExtraProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var header: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.views.item.ItemHeaderProps? | SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.views.item.ItemHeaderProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var image: dynamic /* React.ReactNode? | ItemImageProps? | SemanticShorthandItemFunc<ItemImageProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var meta: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.views.item.ItemMetaProps? | SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.views.item.ItemMetaProps>? */
        get() = definedExternally
        set(value) = definedExternally
}

external interface ItemComponent : RClass<ItemProps> {
    var Content: Any
    var Description: Any
    var Extra: Any
    var Group: Any
    var Header: Any
    var Image: Any
    var Meta: Any
}

@JsName("default")
external var Item: ItemComponent = definedExternally