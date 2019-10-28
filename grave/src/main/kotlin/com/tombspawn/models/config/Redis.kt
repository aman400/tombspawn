package com.tombspawn.models.config

import com.google.gson.annotations.SerializedName

class Redis(@SerializedName("host")
            val host: String? = null,
            @SerializedName("port")
            val port: Int? = null)
