package models.slack

import com.google.gson.annotations.SerializedName

data class BotInfo(
	@field:SerializedName("self") val self: Self? = null,
	@field:SerializedName("team") val team: Team? = null,
	@field:SerializedName("ok") val ok: Boolean = false,
	@field:SerializedName("url") val url: String? = null
) {
	data class Self(
		@field:SerializedName("name") val name: String? = null,
		@field:SerializedName("id") val id: String? = null
	)
	data class Team(
		@field:SerializedName("domain") val domain: String? = null,
		@field:SerializedName("name") val name: String? = null,
		@field:SerializedName("id") val id: String? = null
	)
}
