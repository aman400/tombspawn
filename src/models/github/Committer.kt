package models.github

import com.google.gson.annotations.SerializedName

data class Committer(

	@SerializedName("name")
	val name: String? = null,

	@SerializedName("email")
	val email: String? = null,

	@SerializedName("username")
	val username: String? = null
)