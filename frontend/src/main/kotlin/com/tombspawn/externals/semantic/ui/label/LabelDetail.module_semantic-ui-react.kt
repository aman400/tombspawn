@file:JsModule("semantic-ui-react/dist/commonjs/elements/Label/LabelDetail")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.label

import SemanticShorthandContent
import react.RClass
import react.RProps

external interface LabelDetailProps : StrictLabelDetailProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictLabelDetailProps: RProps {
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

external interface LabelDetailComponent : RClass<LabelDetailProps> {
}

@JsName("default")
external var LabelDetail: LabelDetailComponent = definedExternally