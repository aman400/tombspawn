@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui

import kotlin.js.*
import react.*

external interface HtmlLabelProps : StrictHtmlLabelProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictHtmlLabelProps {
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
}

external interface HtmlIframeProps : StrictHtmlIframeProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictHtmlIframeProps {
    var src: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface HtmlImageProps : StrictHtmlImageProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictHtmlImageProps {
    var src: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface HtmlInputProps : StrictHtmlInputProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictHtmlInputProps {
    var type: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface HtmlSpanProps : StrictHtmlSpanProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictHtmlSpanProps {
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
}

typealias SemanticShorthandItemFunc<TProps> = (component: RComponent<TProps, RState>, props: TProps, children: dynamic /* React.ReactNode | React.ReactNodeArray */) -> ReactElement?

typealias SemanticShorthandCollection<TProps> = Array<dynamic /* React.ReactNode | TProps | com.tombspawn.externals.semantic.ui.SemanticShorthandItemFunc<TProps> */>

typealias SemanticShorthandContent = ReactElement