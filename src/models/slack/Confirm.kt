package models.slack

import com.google.gson.annotations.SerializedName

data class Confirm(

	@field:SerializedName("text")
	val text: String? = null,

	@field:SerializedName("ok_text")
	val okText: String? = null,

	@field:SerializedName("dismiss_text")
	val dismissText: String? = null,

	@field:SerializedName("title")
	val title: String? = null
)