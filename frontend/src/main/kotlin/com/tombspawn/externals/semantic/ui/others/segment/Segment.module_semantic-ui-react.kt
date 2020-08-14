@file:JsModule("semantic-ui-react/dist/commonjs/elements/Segment/Segment")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.others.segment

import com.tombspawn.externals.semantic.ui.SemanticShorthandContent
import react.RClass
import react.RProps
import kotlin.js.*

external interface SegmentProps : StrictSegmentProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictSegmentProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var attached: dynamic /* Boolean? | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var basic: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var circular: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var clearing: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var color: String? /* 'red' | 'orange' | 'yellow' | 'olive' | 'green' | 'teal' | 'blue' | 'violet' | 'purple' | 'pink' | 'brown' | 'grey' | 'black' */
        get() = definedExternally
        set(value) = definedExternally
    var compact: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var content: SemanticShorthandContent?
        get() = definedExternally
        set(value) = definedExternally
    var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var floated: String? /* 'left' | 'right' */
        get() = definedExternally
        set(value) = definedExternally
    var inverted: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var loading: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var padded: dynamic /* Boolean? | String */
        get() = definedExternally
        set(value) = definedExternally
    var placeholder: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var piled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var raised: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var secondary: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var size: String? /* 'mini' | 'tiny' | 'small' | 'large' | 'big' | 'huge' | 'massive' */
        get() = definedExternally
        set(value) = definedExternally
    var stacked: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var tertiary: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var textAlign: String? /* 'left' | 'center' | 'right' | 'justified' */
        get() = definedExternally
        set(value) = definedExternally
    var vertical: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface SegmentComponent : RClass<SegmentProps> {
    var Group: Any
    var Inline: Any
}

@JsName("default")
external var Segment: SegmentComponent = definedExternally