package com.tombspawn.models.slack

import com.google.gson.annotations.SerializedName

enum class ElementType constructor(val type: String) {
    @SerializedName("text")
    TEXT("text"),
    @SerializedName("textarea")
    TEXT_AREA("textarea"),
    @SerializedName("select")
    SELECT("select");

    fun from(value: String): ElementType {
        return values().firstOrNull {
            it.type.equals(value, true)
        } ?: TEXT
    }
}