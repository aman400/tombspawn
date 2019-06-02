package models.slack

import com.google.gson.annotations.SerializedName

data class Action(

	@SerializedName("confirm")
	var confirm: Confirm? = null,

	@SerializedName("name")
	var name: String? = null,

	@SerializedName("text")
	var text: String? = null,

	@SerializedName("type")
	var type: ActionType? = ActionType.DEFAULT,

	@SerializedName("value")
	var value: String? = null,

	@SerializedName("options")
	var options: List<Option>? = null,

	@SerializedName("url")
	var url: String? = null,

	@SerializedName("style")
	var style: ActionStyle = ActionStyle.DEFAULT
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

fun action(block: Action.() -> Unit) = Action().apply(block)