package com.tombspawn.base.common.models

import com.google.gson.annotations.SerializedName

data class GradleTask(
    @SerializedName("id") val id: String,
    @SerializedName("tasks") val tasks: List<String>,
    @SerializedName("output_dir") val outputDir: String
) {
    override fun toString(): String {
        return id
    }
}