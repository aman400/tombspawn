package com.tombspawn.skeleton.models

import com.google.gson.annotations.SerializedName


data class Reference constructor(@SerializedName("name") var name: String,
                     @SerializedName("type") var type: RefType)