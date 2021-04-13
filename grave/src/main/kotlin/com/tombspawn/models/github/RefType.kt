package com.tombspawn.models.github

import com.google.gson.annotations.SerializedName
import com.tombspawn.base.Ref

enum class RefType(val type: String) {
    @SerializedName("branch")
    BRANCH("branch"),
    @SerializedName("tag")
    TAG("tag"),
    @SerializedName("repository")
    REPOSITORY("repository");

    companion object {
        fun from(key: String?): RefType? {
            return values().firstOrNull {
                key?.equals(it.type, true) == true
            }
        }

        fun from(key: Ref): RefType {
            return values().firstOrNull {
                key.type.ordinal == it.ordinal
            } ?: BRANCH
        }
    }
}