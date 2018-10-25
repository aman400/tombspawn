package models.slack

import com.google.gson.annotations.SerializedName

data class Option(

	@field:SerializedName("text")
	val text: String? = null,

	@field:SerializedName("value")
	val value: String? = null
)