package com.tombspawn.base.common.models

import com.google.gson.annotations.SerializedName

data class GradleTask constructor(
    @SerializedName("id") val id: String,
    @SerializedName("tasks") val tasks: List<String>,
    @SerializedName("output_dir") val outputDir: String,
    @SerializedName("timeout") private val _timeout: Long? = 15,
    @SerializedName("exec_dir") val executionDir: String? = null,
    @SerializedName("use_cache") private val _useCache: Boolean? = null
) {
    // Task execution hard timeout
    val timeout: Long
        get() = _timeout ?: 15

    // Whether to user cache for a task or not
    val useCache: Boolean
        get() = _useCache ?: true

    override fun toString(): String {
        return id
    }
}