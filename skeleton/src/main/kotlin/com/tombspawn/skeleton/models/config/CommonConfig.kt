package com.tombspawn.skeleton.models.config

import com.google.gson.annotations.SerializedName

data class CommonConfig(@SerializedName("gradle_path") val gradlePath: String)