@file:JsModule("semantic-ui-react/dist/commonjs/collections/Table/TableHeaderCell")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.table

import react.RClass
import kotlin.js.*

external interface TableHeaderCellProps : StrictTableHeaderCellProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictTableHeaderCellProps : StrictTableCellProps {
    override var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    override var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var sorted: String? /* 'ascending' | 'descending' */
        get() = definedExternally
        set(value) = definedExternally
}


external interface TableHeaderCellComponent : RClass<TableHeaderCellProps>

@JsName("default")
external var TableHeaderCell: TableHeaderCellComponent = definedExternally