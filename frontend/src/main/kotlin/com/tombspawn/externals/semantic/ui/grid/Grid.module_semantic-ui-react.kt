@file:JsModule("semantic-ui-react/dist/commonjs/collections/Grid/Grid")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.grid

import react.RClass
import react.RProps
import kotlin.js.*

external interface GridProps : StrictGridProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictGridProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var celled: dynamic /* Boolean? | String */
        get() = definedExternally
        set(value) = definedExternally
    var centered: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var columns: dynamic /* Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var container: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var divided: dynamic /* Boolean? | String */
        get() = definedExternally
        set(value) = definedExternally
    var doubling: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var inverted: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var padded: dynamic /* Boolean? | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var relaxed: dynamic /* Boolean? | String */
        get() = definedExternally
        set(value) = definedExternally
    var reversed: dynamic /* String? | String | String | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var stackable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var stretched: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var textAlign: String? /* 'left' | 'center' | 'right' | 'justified' */
        get() = definedExternally
        set(value) = definedExternally
    var verticalAlign: String? /* 'top' | 'middle' | 'bottom' */
        get() = definedExternally
        set(value) = definedExternally
}

external interface GridComponent : RClass<GridProps> {
    var Column: Any
    var Row: Any
}

@JsName("default")
external var Grid: GridComponent = definedExternally