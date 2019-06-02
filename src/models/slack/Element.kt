package com.ramukaka.models.slack

import com.google.gson.annotations.SerializedName

class Element(
    @SerializedName("type") var type: ElementType = ElementType.TEXT,
    @SerializedName("label") var label: String = "",
    @SerializedName("name") var name: String = "",
    @SerializedName("placeholder") var placeholder: String? = null,
    @SerializedName("max_length") var maxLength: Int? = null,
    @SerializedName("min_length") var minLength: Int? = null,
    @SerializedName("optional") var optional: Boolean = false,
    @SerializedName("hint") var hint: String? = null,
    @SerializedName("value") var defaultValue: String? = null,
    @SerializedName("subtype") var inputType: InputType? = null,
    // Select options
    @SerializedName("options") var options: MutableList<Option>? = null,
    // Predefined data from slack for selection
    @SerializedName("data_source") var dataSource: DataSource? = null
) {
    operator fun Option.unaryPlus() {
        if(options == null) {
            options = mutableListOf()
        }
        options?.add(this)
    }

    operator fun MutableList<Option>.unaryPlus() {
        if(options == null) {
            options = mutableListOf()
        }
        options?.addAll(this)
    }

    operator fun MutableList<Option>?.invoke(function: () -> Unit) {
        function()
    }

    enum class InputType(val value: String) {
        @SerializedName("email")
        EMAIL("email"),
        @SerializedName("number")
        NUMBER("number"),
        @SerializedName("tel")
        TELEPHONE("tel"),
        @SerializedName("url")
        URL("url")
    }

    enum class DataSource(val value: String) {
        @SerializedName("users")
        USERS("users"),
        @SerializedName("channels")
        CHANNELS("channels"),
        @SerializedName("conversation")
        CONVERSATIONS("conversation"),
        @SerializedName("external")
        EXTERNAL("external")
    }

    class Option(
        @SerializedName("label") val label: String,
        @SerializedName("value") val value: String
    )
}

fun element(block: Element.() -> Unit) = Element().apply(block)