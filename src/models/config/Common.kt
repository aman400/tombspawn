package com.ramukaka.models.config

import com.google.gson.annotations.SerializedName

data class Common(@SerializedName("base_url") val baseUrl: String,
                  @SerializedName("gradle_path") val gradlePath: String)