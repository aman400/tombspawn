@file:JsModule("semantic-ui-react/dist/commonjs/collections/Table/TableRow")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.table

import com.tombspawn.externals.semantic.ui.SemanticShorthandCollection
import react.RClass
import react.RProps
import kotlin.js.*

external interface TableRowProps : StrictTableRowProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictTableRowProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var active: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var cellAs: Any?
        get() = definedExternally
        set(value) = definedExternally
    var cells: SemanticShorthandCollection<TableCellProps>?
        get() = definedExternally
        set(value) = definedExternally
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var error: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var negative: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var positive: Boolean?
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
}


external interface TableRowComponent : RClass<TableRowProps>

@JsName("default")
external var TableRow: TableRowComponent = definedExternally