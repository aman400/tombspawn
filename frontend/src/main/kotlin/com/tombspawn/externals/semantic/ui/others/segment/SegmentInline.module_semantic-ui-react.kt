@file:JsModule("semantic-ui-react/dist/commonjs/elements/Segment/SegmentInline")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.others.segment

import com.tombspawn.externals.semantic.ui.SemanticShorthandContent
import com.tombspawn.externals.semantic.ui.others.CheckboxProps
import react.RClass
import react.RProps
import kotlin.js.*

external interface SegmentInlineProps : StrictSegmentInlineProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictSegmentInlineProps: RProps {
    var `as`: Any?
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
}

external interface SegmentInlineComponent : RClass<SegmentInlineProps>

@JsName("default")
external var SegmentInline: SegmentInlineComponent = definedExternally