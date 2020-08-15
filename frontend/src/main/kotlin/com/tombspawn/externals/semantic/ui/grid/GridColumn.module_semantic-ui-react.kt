@file:JsModule("semantic-ui-react/dist/commonjs/collections/Grid/GridColumn")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.grid

import react.RClass
import react.RProps
import styled.StyledProps
import kotlin.js.*

external interface GridColumnProps : StrictGridColumnProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictGridColumnProps: StyledProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    override var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var color: String? /* 'red' | 'orange' | 'yellow' | 'olive' | 'green' | 'teal' | 'blue' | 'violet' | 'purple' | 'pink' | 'brown' | 'grey' | 'black' */
        get() = definedExternally
        set(value) = definedExternally
    var computer: dynamic /* Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var floated: String? /* 'left' | 'right' */
        get() = definedExternally
        set(value) = definedExternally
    var largeScreen: dynamic /* Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var mobile: dynamic /* Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var only: dynamic /* String? | String | String | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var stretched: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var tablet: dynamic /* Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var textAlign: String? /* 'left' | 'center' | 'right' | 'justified' */
        get() = definedExternally
        set(value) = definedExternally
    var verticalAlign: String? /* 'top' | 'middle' | 'bottom' */
        get() = definedExternally
        set(value) = definedExternally
    var widescreen: dynamic /* Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var width: dynamic /* Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
}

external interface GridColumnComponent : RClass<GridColumnProps> {
    var Column: Any
    var Row: Any
}

@JsName("default")
external var GridColumn: GridColumnComponent = definedExternally