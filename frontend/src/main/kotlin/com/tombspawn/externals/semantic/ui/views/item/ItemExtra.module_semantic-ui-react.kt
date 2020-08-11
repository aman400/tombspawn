@file:JsModule("semantic-ui-react/dist/commonjs/views/Item/ItemExtra")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.views.item

import SemanticShorthandContent
import react.RClass
import react.RProps
import kotlin.js.*

external interface ItemExtraProps : StrictItemExtraProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictItemExtraProps: RProps {
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
}

external interface ItemExtraComponent : RClass<ItemExtraProps>

@JsName("default")
external var ItemExtra: ItemExtraComponent = definedExternally