@file:JsModule("semantic-ui-react/dist/commonjs/collections/Table/TableCell")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.table

import SemanticShorthandContent
import react.RClass
import react.RProps
import kotlin.js.*

external interface TableCellProps : StrictTableCellProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictTableCellProps: RProps {
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
    var collapsing: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var content: SemanticShorthandContent?
        get() = definedExternally
        set(value) = definedExternally
    var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var error: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var icon: dynamic /* React.ReactNode? | IconProps? | SemanticShorthandItemFunc<IconProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var negative: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var positive: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var selectable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var singleLine: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var textAlign: String? /* 'center' | 'left' | 'right' */
        get() = definedExternally
        set(value) = definedExternally
    var verticalAlign: String? /* 'top' | 'middle' | 'bottom' */
        get() = definedExternally
        set(value) = definedExternally
    var warning: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var width: dynamic /* Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
}

external interface TableCellComponent : RClass<TableCellProps>

@JsName("default")
external var TableCell: TableCellComponent = definedExternally