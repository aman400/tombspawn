@file:JsModule("semantic-ui-react/dist/commonjs/elements/Grid/GridRow")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.grid

import react.RClass
import react.RProps
import kotlin.js.*

external interface GridRowProps : StrictGridRowProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictGridRowProps: RProps {
    var `as`: Any?
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
    var color: String? /* 'red' | 'orange' | 'yellow' | 'olive' | 'green' | 'teal' | 'blue' | 'violet' | 'purple' | 'pink' | 'brown' | 'grey' | 'black' */
        get() = definedExternally
        set(value) = definedExternally
    var columns: dynamic /* Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var divided: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var only: dynamic /* String? | String | String | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var reversed: dynamic /* String? | String | String | String | String | String | String */
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

external interface GridRowComponent : RClass<GridRowProps> {
    var Column: Any
    var Row: Any
}

@JsName("default")
external var GridRow: GridRowComponent = definedExternally