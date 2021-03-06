package com.tombspawn.models.github

import com.google.gson.annotations.SerializedName

data class Pusher(

	@SerializedName("name")
	val name: String? = null,

	@SerializedName("email")
	val email: String? = null
)