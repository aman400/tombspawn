@file:JsModule("semantic-ui-react/dist/commonjs/collections/Table/Table")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.table

import com.tombspawn.externals.semantic.ui.SemanticShorthandCollection
import react.RClass
import react.RProps
import kotlin.js.*

external interface TableProps : StrictTableProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictTableProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var attached: dynamic /* Boolean? | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var basic: dynamic /* Boolean? | String */
        get() = definedExternally
        set(value) = definedExternally
    var celled: dynamic /* Boolean? | String */
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
    var color: String? /* 'red' | 'orange' | 'yellow' | 'olive' | 'green' | 'teal' | 'blue' | 'violet' | 'purple' | 'pink' | 'brown' | 'grey' | 'black' */
        get() = definedExternally
        set(value) = definedExternally
    var columns: dynamic /* Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var compact: dynamic /* Boolean? | String */
        get() = definedExternally
        set(value) = definedExternally
    var definition: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var fixed: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var footerRow: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.table.TableRowProps? | com.tombspawn.externals.semantic.ui.SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.table.TableRowProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var headerRow: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.table.TableRowProps? | com.tombspawn.externals.semantic.ui.SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.table.TableRowProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var headerRows: SemanticShorthandCollection<TableRowProps>?
        get() = definedExternally
        set(value) = definedExternally
    var inverted: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var padded: dynamic /* Boolean? | String */
        get() = definedExternally
        set(value) = definedExternally
    var renderBodyRow: ((data: Any, index: Number) -> Any)?
        get() = definedExternally
        set(value) = definedExternally
    var selectable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var singleLine: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var size: String? /* 'small' | 'large' */
        get() = definedExternally
        set(value) = definedExternally
    var sortable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var stackable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var striped: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var structured: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var tableData: Array<Any>?
        get() = definedExternally
        set(value) = definedExternally
    var textAlign: String? /* 'center' | 'left' | 'right' */
        get() = definedExternally
        set(value) = definedExternally
    var unstackable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var verticalAlign: String? /* 'top' | 'middle' | 'bottom' */
        get() = definedExternally
        set(value) = definedExternally
}

external interface TableComponent : RClass<TableProps> {
    var Body: Any
    var Cell: Any
    var Footer: Any
    var Header: Any
    var HeaderCell: Any
    var Row: Any
}

@JsName("default")
external var Table: TableComponent = definedExternally