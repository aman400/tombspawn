package com.tombspawn.models.slack

import com.google.gson.annotations.SerializedName

enum class ElementType(val type: String) {
    @SerializedName("text")
    TEXT("text"),
    @SerializedName("textarea")
    TEXT_AREA("textarea"),
    @SerializedName("select")
    SELECT("select")
}