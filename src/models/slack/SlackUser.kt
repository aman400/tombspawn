package models.slack

import com.google.gson.annotations.SerializedName

data class SlackUser(

	@SerializedName("name")
	val name: String? = null,

	@SerializedName("id")
	val id: String? = null,

	@SerializedName("is_bot")
	val bot: Boolean? = false,

	@SerializedName("deleted")
	val deleted: Boolean? = false,

	@SerializedName("profile")
	val profile: UserProfile? = null
)