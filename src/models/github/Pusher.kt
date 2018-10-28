package models.github

import com.google.gson.annotations.SerializedName

data class Pusher(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("email")
	val email: String? = null
)