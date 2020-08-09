@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

import kotlin.js.*
import kotlin.js.Json
import org.khronos.webgl.*
import org.w3c.dom.*
import org.w3c.dom.events.*
import org.w3c.dom.parsing.*
import org.w3c.dom.svg.*
import org.w3c.dom.url.*
import org.w3c.fetch.*
import org.w3c.files.*
import org.w3c.notifications.*
import org.w3c.performance.*
import org.w3c.workers.*
import org.w3c.xhr.*
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

external interface HtmlInputrops : StrictHtmlInputrops {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictHtmlInputrops {
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

typealias SemanticShorthandCollection<TProps> = Array<dynamic /* React.ReactNode | TProps | SemanticShorthandItemFunc<TProps> */>

typealias SemanticShorthandContent = ReactElement