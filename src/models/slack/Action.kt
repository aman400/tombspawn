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
	var actionOptions: MutableList<Option>? = null,

	@SerializedName("url")
	var url: String? = null,

	@SerializedName("style")
	var style: ActionStyle = ActionStyle.DEFAULT
) {

	operator fun Option.unaryPlus() {
		if (actionOptions == null) {
			actionOptions = mutableListOf(this)
		} else {
			actionOptions?.add(this)
		}
	}

	operator fun MutableList<Option>.unaryPlus() {
		if (actionOptions == null) {
			actionOptions = mutableListOf()
		}
		actionOptions?.addAll(this)
	}

	operator fun MutableList<Option>?.invoke(function: MutableList<Option>?.() -> Unit) {
		function()
	}

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

	data class Option(

		@SerializedName("text")
		val text: String? = null,

		@SerializedName("value")
		val value: String? = null
	)
}

fun actionOption(block: Action.Option.() -> Unit) = Action.Option().apply(block)

fun action(block: Action.() -> Unit) = Action().apply(block)