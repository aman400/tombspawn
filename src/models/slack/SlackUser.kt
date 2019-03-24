package models.slack

import com.google.gson.annotations.SerializedName

data class SlackUser(

	@SerializedName("name")
	val name: String? = null,

	@SerializedName("id")
	val id: String? = null
)