package com.tombspawn.skeleton.models

import com.google.gson.annotations.SerializedName

enum class RefType(val type: String) {
    @SerializedName("branch")
    BRANCH("branch"),
    @SerializedName("tag")
    TAG("tag"),
    @SerializedName("repository")
    REPOSITORY("repository")
}