package models.slack

import com.google.gson.annotations.SerializedName

data class Action(

	@field:SerializedName("confirm")
	val confirm: Confirm? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("text")
	val text: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("value")
	val value: String? = null,

	@field:SerializedName("options")
	val options: List<Option>? = null
)