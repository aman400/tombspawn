@file:JsModule("semantic-ui-react/dist/commonjs/views/Item/ItemGroup")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.views.item

import com.tombspawn.externals.semantic.ui.SemanticShorthandCollection
import com.tombspawn.externals.semantic.ui.SemanticShorthandContent
import react.RClass
import react.RProps
import kotlin.js.*

external interface ItemGroupProps : StrictItemGroupProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictItemGroupProps: RProps {
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
    var divided: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var items: SemanticShorthandCollection<ItemProps>?
        get() = definedExternally
        set(value) = definedExternally
    var link: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var relaxed: dynamic /* Boolean? | String */
        get() = definedExternally
        set(value) = definedExternally
    var unstackable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface ItemGroupComponent : RClass<ItemGroupProps>

@JsName("default")
external var ItemGroup: ItemGroupComponent = definedExternally