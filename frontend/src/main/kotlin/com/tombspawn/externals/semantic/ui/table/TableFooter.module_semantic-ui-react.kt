@file:JsModule("semantic-ui-react/dist/commonjs/collections/Table/TableFooter")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.table

import react.RClass
import kotlin.js.*

external interface TableFooterProps : StrictTableFooterProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictTableFooterProps : StrictTableHeaderProps {
    override var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
}


external interface TableFooterComponent : RClass<TableFooterProps>

@JsName("default")
external var TableFooter: TableFooterComponent = definedExternally