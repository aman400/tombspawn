package models.slack

import com.google.gson.annotations.SerializedName

data class Confirm(

	@SerializedName("text")
	var text: String? = null,

	@SerializedName("ok_text")
	var okText: String? = null,

	@SerializedName("dismiss_text")
	var dismissText: String? = null,

	@SerializedName("title")
	var title: String? = null
)

fun confirm(block: Confirm.() -> Unit) = Confirm().apply(block)