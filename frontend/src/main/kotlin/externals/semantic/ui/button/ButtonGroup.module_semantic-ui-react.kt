@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.semantic.ui.button

import SemanticShorthandCollection
import SemanticShorthandContent
import kotlin.js.*

external interface ButtonGroupProps : StrictButtonGroupProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictButtonGroupProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var attached: dynamic /* Boolean? | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var basic: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var buttons: SemanticShorthandCollection<ButtonProps>?
        get() = definedExternally
        set(value) = definedExternally
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
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
    var floated: String? /* 'left' | 'right' */
        get() = definedExternally
        set(value) = definedExternally
    var fluid: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var icon: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var inverted: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var labeled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var negative: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var positive: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var primary: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var secondary: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var size: String? /* 'mini' | 'tiny' | 'small' | 'medium' | 'large' | 'big' | 'huge' | 'massive' */
        get() = definedExternally
        set(value) = definedExternally
    var toggle: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var vertical: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var widths: dynamic /* Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | Number | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
}