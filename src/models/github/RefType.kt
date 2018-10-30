package com.ramukaka.models.github

import com.google.gson.annotations.SerializedName

enum class RefType(val type: String) {
    @field:SerializedName("branch")
    BRANCH("branch"),
    @field:SerializedName("tag")
    TAG("tag"),
    @field:SerializedName("repository")
    REPOSITORY("repository")
}