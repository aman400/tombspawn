@file:JsModule("semantic-ui-react/dist/commonjs/elements/Button")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.semantic.ui.button

import SemanticShorthandContent
import org.w3c.dom.events.MouseEvent
import react.RClass
import react.RComponent
import react.RProps
import react.RState
import kotlin.js.*

external interface ButtonProps : StrictButtonProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictButtonProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var active: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var animated: dynamic /* Boolean? | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var attached: dynamic /* Boolean? | String | String | String | String */
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
    var color: String? /* 'red' | 'orange' | 'yellow' | 'olive' | 'green' | 'teal' | 'blue' | 'violet' | 'purple' | 'pink' | 'brown' | 'grey' | 'black' | 'facebook' | 'google plus' | 'vk' | 'twitter' | 'linkedin' | 'instagram' | 'youtube' */
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
    var fluid: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var icon: dynamic /* Boolean? | React.ReactNode? | IconProps? | SemanticShorthandItemFunc<IconProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var inverted: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var label: dynamic /* React.ReactNode? | LabelProps? | SemanticShorthandItemFunc<LabelProps>? */
        get() = definedExternally
        set(value) = definedExternally
    var labelPosition: String? /* 'right' | 'left' */
        get() = definedExternally
        set(value) = definedExternally
    var loading: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var negative: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var onClick: ((event: (event: MouseEvent) -> Unit, data: ButtonProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally

//    var onClick: ((event: React.MouseEvent<HTMLButtonElement>, data: externals.semantic.ui.button.ButtonProps) -> Unit)?

    var positive: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var primary: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var role: String?
        get() = definedExternally
        set(value) = definedExternally
    var secondary: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var size: String? /* 'mini' | 'tiny' | 'small' | 'medium' | 'large' | 'big' | 'huge' | 'massive' */
        get() = definedExternally
        set(value) = definedExternally
    var tabIndex: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
    var toggle: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

abstract external class ButtonComponent : RClass<ButtonProps> {
    override var displayName: String? = definedExternally
    var focus: () -> Unit
    var Content: Any
    var Group: Any
    var Or: Any
}

@JsName("default")
external var Button: ButtonComponent = definedExternally
