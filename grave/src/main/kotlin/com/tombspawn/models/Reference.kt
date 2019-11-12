package com.tombspawn.models

import com.google.gson.annotations.SerializedName
import com.tombspawn.models.github.RefType


data class Reference(@SerializedName("name") var name: String,
                     @SerializedName("type") var type: RefType)