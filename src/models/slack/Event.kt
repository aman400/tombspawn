package models.slack

import com.google.gson.annotations.SerializedName

data class Event(

	@SerializedName("event_ts")
	val eventTimestamp: String? = null,

	@SerializedName("channel")
	val channel: String? = null,

	@SerializedName("text")
	val text: String? = null,

	@SerializedName("type")
	val type: String? = null,

	@SerializedName("user")
	val user: String? = null,

	@SerializedName("ts")
	val ts: String? = null
)