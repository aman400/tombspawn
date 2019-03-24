package models.slack

import com.google.gson.annotations.SerializedName

data class Option(

	@SerializedName("text")
	val text: String? = null,

	@SerializedName("value")
	val value: String? = null
)