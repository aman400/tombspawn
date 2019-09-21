package com.ramukaka.models.config

import com.google.gson.annotations.SerializedName

data class JWT(@SerializedName("domain") val domain: String,
          @SerializedName("audience") val audience: String,
          @SerializedName("realm") val realm: String,
          @SerializedName("secret") val secret: String)