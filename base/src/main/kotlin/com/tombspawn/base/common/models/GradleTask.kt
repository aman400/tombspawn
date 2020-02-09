package com.tombspawn.base.common.models

import com.google.gson.annotations.SerializedName

data class GradleTask constructor(
    @SerializedName("id") val id: String,
    @SerializedName("tasks") val tasks: List<String>,
    @SerializedName("output_dir") val outputDir: String,
    @SerializedName("use_cache") val useCache: Boolean = true
) {
    override fun toString(): String {
        return id
    }
}