package models.github

import com.google.gson.annotations.SerializedName
import com.ramukaka.models.github.RefType

data class Payload(

	@SerializedName("compare")
	val compare: String? = null,

	@SerializedName("head_commit")
	val headCommit: HeadCommit? = null,

	@SerializedName("pusher")
	val pusher: Pusher? = null,

	@SerializedName("before")
	val before: String? = null,

	@SerializedName("created")
	val created: Boolean? = null,

	@SerializedName("forced")
	val forced: Boolean? = null,

	@SerializedName("base_ref")
	val baseRef: String? = null,

	@SerializedName("repository")
	val repository: Repository? = null,

	@SerializedName("ref")
	val ref: String? = null,

	@SerializedName("ref_type")
	val refType: RefType? = null,

	@SerializedName("deleted")
	val deleted: Boolean? = null,

	@SerializedName("sender")
	val sender: Sender? = null,

	@SerializedName("commits")
	val commits: List<Any?>? = null,

	@SerializedName("after")
	val after: String? = null
)