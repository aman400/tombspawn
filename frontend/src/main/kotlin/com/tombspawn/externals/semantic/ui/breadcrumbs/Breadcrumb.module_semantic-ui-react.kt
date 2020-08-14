@file:JsModule("semantic-ui-react/dist/commonjs/collections/Breadcrumb/Breadcrumb")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.breadcrumbs

import com.tombspawn.externals.semantic.ui.SemanticShorthandCollection
import com.tombspawn.externals.semantic.ui.SemanticShorthandContent
import kotlin.js.*
import react.RClass
import react.RProps

external interface BreadcrumbProps : StrictBreadcrumbProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictBreadcrumbProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var divider: SemanticShorthandContent?
        get() = definedExternally
        set(value) = definedExternally
    var icon: dynamic /* React.ReactNode? | com.tombspawn.externals.semantic.ui.others.icon.IconProps? | com.tombspawn.externals.semantic.ui.SemanticShorthandItemFunc<com.tombspawn.externals.semantic.ui.others.icon.IconProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var sections: SemanticShorthandCollection<BreadcrumbSectionProps>?
        get() = definedExternally
        set(value) = definedExternally
    var size: String? /* 'mini' | 'tiny' | 'small' | 'large' | 'big' | 'huge' | 'massive' */
        get() = definedExternally
        set(value) = definedExternally
}

external interface BreadcrumbComponent : RClass<BreadcrumbProps> {
    var Divider: BreadcrumbDividerComponent?
    var Section: BreadcrumbSectionComponent?
}

@JsName("default")
external var Breadcrumb: BreadcrumbComponent = definedExternally
