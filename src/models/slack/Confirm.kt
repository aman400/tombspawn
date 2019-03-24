package models.slack

import com.google.gson.annotations.SerializedName

data class Confirm(

	@SerializedName("text")
	val text: String? = null,

	@SerializedName("ok_text")
	val okText: String? = null,

	@SerializedName("dismiss_text")
	val dismissText: String? = null,

	@SerializedName("title")
	val title: String? = null
)