@file:JsModule("semantic-ui-react/dist/commonjs/collections/Breadcrumb/BreadcrumbSection")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.breadcrumbs

import com.tombspawn.externals.semantic.ui.SemanticShorthandContent
import kotlin.js.*
import org.w3c.dom.events.*
import react.RClass
import react.RProps

external interface BreadcrumbSectionProps : StrictBreadcrumbSectionProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictBreadcrumbSectionProps: RProps {
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
    var content: SemanticShorthandContent?
        get() = definedExternally
        set(value) = definedExternally
    var href: String?
        get() = definedExternally
        set(value) = definedExternally
    var link: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var onClick: ((event: Event, data: BreadcrumbSectionProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
}


external interface BreadcrumbSectionComponent : RClass<BreadcrumbSectionProps>

@JsName("default")
external var BreadcrumbSection: BreadcrumbSectionComponent = definedExternally