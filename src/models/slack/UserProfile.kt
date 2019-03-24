package models.slack

import com.google.gson.annotations.SerializedName

data class UserProfile(

	@SerializedName("image_24")
	val imageUrl: String? = null,

	@SerializedName("real_name_normalized")
	val name: String? = null,

	@SerializedName("avatar_hash")
	val avatarHash: String? = null,

	@SerializedName("email")
	val email: String? = null
)