package com.ramukaka.userdata.users

import com.google.gson.annotations.SerializedName

data class User(@SerializedName("id") val id: String? = null, @SerializedName("name") val name: String? = null,
                @SerializedName("email") val email: String? = null)