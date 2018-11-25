package models.slack

import com.google.gson.annotations.SerializedName

data class Action(

	@SerializedName("confirm")
	val confirm: Confirm? = null,

	@SerializedName("name")
	val name: String? = null,

	@SerializedName("text")
	val text: String? = null,

	@SerializedName("type")
	val type: ActionType? = ActionType.DEFAULT,

	@SerializedName("value")
	val value: String? = null,

	@SerializedName("options")
	val options: List<Option>? = null,

	@SerializedName("url")
	val url: String? = null,

	@SerializedName("style")
	val style: ActionStyle = ActionStyle.DEFAULT
) {
	enum class ActionType(val value: String) {
		@SerializedName("default")
		DEFAULT("default"),
		@SerializedName("button")
		BUTTON("button"),
		@SerializedName("select")
		SELECT("select")
	}

	enum class ActionStyle(val value: String) {
		@SerializedName("default")
		DEFAULT("default"),
		@SerializedName("danger")
		DANGER("danger"),
		@SerializedName("primary")
		PRIMARY("primary")
	}
}