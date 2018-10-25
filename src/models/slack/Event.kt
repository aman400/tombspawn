package models.slack

import com.google.gson.annotations.SerializedName

data class Event(

	@field:SerializedName("event_ts")
	val eventTimestamp: String? = null,

	@field:SerializedName("channel")
	val channel: String? = null,

	@field:SerializedName("text")
	val text: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("user")
	val user: String? = null,

	@field:SerializedName("ts")
	val ts: String? = null
)