package models

import com.google.gson.annotations.SerializedName

data class Payload(

	@field:SerializedName("compare")
	val compare: String? = null,

	@field:SerializedName("head_commit")
	val headCommit: HeadCommit? = null,

	@field:SerializedName("pusher")
	val pusher: Pusher? = null,

	@field:SerializedName("before")
	val before: String? = null,

	@field:SerializedName("created")
	val created: Boolean? = null,

	@field:SerializedName("forced")
	val forced: Boolean? = null,

	@field:SerializedName("base_ref")
	val baseRef: String? = null,

	@field:SerializedName("repository")
	val repository: Repository? = null,

	@field:SerializedName("ref")
	val ref: String? = null,

	@field:SerializedName("deleted")
	val deleted: Boolean? = null,

	@field:SerializedName("sender")
	val sender: Sender? = null,

	@field:SerializedName("commits")
	val commits: List<Any?>? = null,

	@field:SerializedName("after")
	val after: String? = null
)