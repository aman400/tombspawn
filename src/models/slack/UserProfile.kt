package models.slack

import com.google.gson.annotations.SerializedName

data class UserProfile(

	@field:SerializedName("image_24")
	val imageUrl: String? = null,

	@field:SerializedName("real_name_normalized")
	val name: String? = null,

	@field:SerializedName("avatar_hash")
	val avatarHash: String? = null,

	@field:SerializedName("email")
	val email: String? = null
)