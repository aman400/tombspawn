@file:JsModule("semantic-ui-react/dist/commonjs/modules/Dropdown/Dropdown")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.tombspawn.externals.semantic.ui.others.dropdown

import com.tombspawn.externals.semantic.ui.label.LabelProps
import kotlin.js.*
import org.w3c.dom.events.*
import react.RClass
import react.RProps

external interface DropdownProps : StrictDropdownProps {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface StrictDropdownProps: RProps {
    var `as`: Any?
        get() = definedExternally
        set(value) = definedExternally
    var additionLabel: dynamic /* Number? | String? | React.ReactNode? */
        get() = definedExternally
        set(value) = definedExternally
    var additionPosition: String? /* 'top' | 'bottom' */
        get() = definedExternally
        set(value) = definedExternally
    var allowAdditions: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var basic: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var button: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var clearable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var closeOnBlur: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var closeOnEscape: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var closeOnChange: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var compact: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var deburr: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var defaultOpen: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var defaultSearchQuery: String?
        get() = definedExternally
        set(value) = definedExternally
    var defaultSelectedLabel: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
    var defaultUpward: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var defaultValue: dynamic /* String? | Number? | Boolean? | Array<Boolean>? */
        get() = definedExternally
        set(value) = definedExternally
    var direction: String? /* 'left' | 'right' */
        get() = definedExternally
        set(value) = definedExternally
    var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var error: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var floating: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var fluid: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var header: Any?
        get() = definedExternally
        set(value) = definedExternally
    var icon: Any?
        get() = definedExternally
        set(value) = definedExternally
    var inline: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var item: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var labeled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var lazyLoad: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var loading: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var minCharacters: Number?
        get() = definedExternally
        set(value) = definedExternally
    var multiple: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var noResultsMessage: Any?
        get() = definedExternally
        set(value) = definedExternally
    var onAddItem: ((event: Event, data: DropdownProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onBlur: ((event: Event, data: DropdownProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onChange: ((event: Event, data: DropdownProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onClick: ((event: Event, data: DropdownProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onClose: ((event: Event, data: DropdownProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onFocus: ((event: Event, data: DropdownProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onLabelClick: ((event: Event, data: LabelProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onMouseDown: ((event: Event, data: DropdownProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onOpen: ((event: Event, data: DropdownProps) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onSearchChange: ((event: Event, data: DropdownOnSearchChangeData) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var open: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var openOnFocus: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var options: Array<DropdownItemProps>?
        get() = definedExternally
        set(value) = definedExternally
    var placeholder: String?
        get() = definedExternally
        set(value) = definedExternally
    var pointing: dynamic /* Boolean? | String | String | String | String | String | String | String | String */
        get() = definedExternally
        set(value) = definedExternally
    var renderLabel: ((item: DropdownItemProps, index: Number, defaultLabelProps: LabelProps) -> Any)?
        get() = definedExternally
        set(value) = definedExternally
    var scrolling: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var search: dynamic /* Boolean? | ((options: Array<com.tombspawn.externals.semantic.ui.others.dropdown.DropdownItemProps>, value: String) -> Array<com.tombspawn.externals.semantic.ui.others.dropdown.DropdownItemProps>)? */
        get() = definedExternally
        set(value) = definedExternally
    var searchInput: Any?
        get() = definedExternally
        set(value) = definedExternally
    var searchQuery: String?
        get() = definedExternally
        set(value) = definedExternally
    var selectOnBlur: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var selectOnNavigation: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var selectedLabel: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
    var selection: Any?
        get() = definedExternally
        set(value) = definedExternally
    var simple: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var tabIndex: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
    var text: String?
        get() = definedExternally
        set(value) = definedExternally
    var trigger: Any?
        get() = definedExternally
        set(value) = definedExternally
    var value: dynamic /* Boolean? | Number? | String? | Array<String>? */
        get() = definedExternally
        set(value) = definedExternally
    var upward: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var wrapSelection: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface DropdownOnSearchChangeData : DropdownProps

external interface DropdownComponent : RClass<DropdownProps> {
    var Divider: Any
    var Header: Any
    var Item: Any
    var Menu: Any
    var SearchInput: Any
}

@JsName("default")
external var Dropdown: DropdownComponent = definedExternally