package com.tombspawn.skeleton.models.config

import com.google.gson.annotations.SerializedName

data class CommonConfig constructor(@SerializedName("gradle_path") val gradlePath: String)