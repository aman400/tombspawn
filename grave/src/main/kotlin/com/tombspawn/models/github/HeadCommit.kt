package com.tombspawn.models.github

import com.google.gson.annotations.SerializedName
import com.tombspawn.models.github.Author
import com.tombspawn.models.github.Committer

data class HeadCommit(

    @SerializedName("committer")
	val committer: Committer? = null,

    @SerializedName("removed")
	val removed: List<Any?>? = null,

    @SerializedName("tree_id")
	val treeId: String? = null,

    @SerializedName("added")
	val added: List<String?>? = null,

    @SerializedName("author")
	val author: Author? = null,

    @SerializedName("distinct")
	val distinct: Boolean? = null,

    @SerializedName("modified")
	val modified: List<Any?>? = null,

    @SerializedName("id")
	val id: String? = null,

    @SerializedName("message")
	val message: String? = null,

    @SerializedName("url")
	val url: String? = null,

    @SerializedName("timestamp")
	val timestamp: String? = null
)