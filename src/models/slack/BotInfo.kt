package models.slack

import com.google.gson.annotations.SerializedName

data class BotInfo(
	@SerializedName("self") val self: Self? = null,
	@SerializedName("team") val team: Team? = null,
	@SerializedName("ok") val ok: Boolean = false,
	@SerializedName("url") val url: String? = null
) {
	data class Self(
		@SerializedName("name") val name: String? = null,
		@SerializedName("id") val id: String? = null
	)
	data class Team(
		@SerializedName("domain") val domain: String? = null,
		@SerializedName("name") val name: String? = null,
		@SerializedName("id") val id: String? = null
	)
}
