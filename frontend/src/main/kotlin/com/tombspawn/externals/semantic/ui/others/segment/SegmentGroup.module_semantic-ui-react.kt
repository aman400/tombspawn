@file:JsModule("semantic-ui-react/dist/commonjs/elements/Segment/SegmentGroup")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.others.segment

import com.tombspawn.externals.semantic.ui.SemanticShorthandContent
import react.RClass
import react.RProps
import kotlin.js.*

external interface SegmentGroupProps : StrictSegmentGroupProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictSegmentGroupProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var compact: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var content: SemanticShorthandContent?
        get() = definedExternally
        set(value) = definedExternally
    var horizontal: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var piled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var raised: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var size: String? /* 'mini' | 'tiny' | 'small' | 'large' | 'big' | 'huge' | 'massive' */
        get() = definedExternally
        set(value) = definedExternally
    var stacked: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface SegmentGroupComponent : RClass<SegmentGroupProps>

@JsName("default")
external var SegmentGroup: SegmentGroupComponent = definedExternally