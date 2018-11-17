package com.ramukaka.models.slack

import com.google.gson.annotations.SerializedName

class Element(
    @SerializedName("type") val type: ElementType,
    @SerializedName("label") val label: String,
    @SerializedName("name") val name: String,
    @SerializedName("placeholder") val placeholder: String? = null,
    @SerializedName("max_length") val maxLength: Int? = null,
    @SerializedName("min_length") val minLength: Int? = null,
    @SerializedName("optional") val optional: Boolean = false,
    @SerializedName("hint") val hint: String? = null,
    @SerializedName("value") val defaultValue: String? = null,
    @SerializedName("subtype") val inputType: InputType? = null,
    // Select options
    @SerializedName("options") val options: List<Option>? = null,
    // Predefined data from slack for selection
    @SerializedName("data_source") val dataSource: DataSource? = null
) {
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