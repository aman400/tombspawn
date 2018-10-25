package models.slack

import com.google.gson.annotations.SerializedName

data class SlackUser(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: String? = null
)