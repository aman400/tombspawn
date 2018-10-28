package models.github

import com.google.gson.annotations.SerializedName

data class HeadCommit(

    @field:SerializedName("committer")
	val committer: Committer? = null,

    @field:SerializedName("removed")
	val removed: List<Any?>? = null,

    @field:SerializedName("tree_id")
	val treeId: String? = null,

    @field:SerializedName("added")
	val added: List<String?>? = null,

    @field:SerializedName("author")
	val author: Author? = null,

    @field:SerializedName("distinct")
	val distinct: Boolean? = null,

    @field:SerializedName("modified")
	val modified: List<Any?>? = null,

    @field:SerializedName("id")
	val id: String? = null,

    @field:SerializedName("message")
	val message: String? = null,

    @field:SerializedName("url")
	val url: String? = null,

    @field:SerializedName("timestamp")
	val timestamp: String? = null
)