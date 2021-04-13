package com.tombspawn.models.bitbucket

import com.google.gson.annotations.SerializedName
import com.tombspawn.models.Reference
import com.tombspawn.models.config.App
import com.tombspawn.models.github.RefType

data class BitbucketResponse(

	@field:SerializedName("actor")
	val actor: Actor? = null,

	@field:SerializedName("repository")
	val repository: Repository? = null,

	@field:SerializedName("push")
	val push: Push? = null
)

data class Commits(

	@field:SerializedName("href")
	val href: String? = null
)

data class Rendered(
	val any: Any? = null
)

data class CommitData(

	@field:SerializedName("default_merge_strategy")
	val defaultMergeStrategy: String? = null,

	@field:SerializedName("merge_strategies")
	val mergeStrategies: List<String>? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("links")
	val links: Links? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("target")
	val target: Target? = null
)

fun CommitData?.getData(app: App?): Pair<App, Reference>? {
	return this?.let { data ->
		val refType = RefType.from(data.type)
		val name = data.name
		if(app != null && refType != null && name != null) {
			Pair(app, Reference(name, refType))
		} else null
	}
}

data class Statuses(

	@field:SerializedName("href")
	val href: String? = null
)

data class Project(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("links")
	val links: Links? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("uuid")
	val uuid: String? = null,

	@field:SerializedName("key")
	val key: String? = null
)

data class Self(

	@field:SerializedName("href")
	val href: String? = null
)

data class Owner(

	@field:SerializedName("account_id")
	val accountId: String? = null,

	@field:SerializedName("nickname")
	val nickname: String? = null,

	@field:SerializedName("links")
	val links: Links? = null,

	@field:SerializedName("display_name")
	val displayName: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("uuid")
	val uuid: String? = null
)

data class CommitsItem(

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("summary")
	val summary: Summary? = null,

	@field:SerializedName("rendered")
	val rendered: Rendered? = null,

	@field:SerializedName("author")
	val author: Author? = null,

	@field:SerializedName("links")
	val links: Links? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("hash")
	val hash: String? = null,

	@field:SerializedName("properties")
	val properties: Properties? = null,

	@field:SerializedName("parents")
	val parents: List<ParentsItem>? = null
)

data class Links(

	@field:SerializedName("commits")
	val commits: Commits? = null,

	@field:SerializedName("html")
	val html: Html? = null,

	@field:SerializedName("self")
	val self: Self? = null
)

data class Avatar(

	@field:SerializedName("href")
	val href: String? = null
)

data class Author(

	@field:SerializedName("raw")
	val raw: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("user")
	val user: User? = null
)

data class Approve(

	@field:SerializedName("href")
	val href: String? = null
)

data class ParentsItem(

	@field:SerializedName("links")
	val links: Links? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("hash")
	val hash: String? = null
)

data class Workspace(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("links")
	val links: Links? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("uuid")
	val uuid: String? = null,

	@field:SerializedName("slug")
	val slug: String? = null
)

data class Patch(

	@field:SerializedName("href")
	val href: String? = null
)

data class Target(

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("summary")
	val summary: Summary? = null,

	@field:SerializedName("rendered")
	val rendered: Rendered? = null,

	@field:SerializedName("author")
	val author: Author? = null,

	@field:SerializedName("links")
	val links: Links? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("hash")
	val hash: String? = null,

	@field:SerializedName("properties")
	val properties: Properties? = null,

	@field:SerializedName("parents")
	val parents: List<ParentsItem>? = null
)

data class Diff(

	@field:SerializedName("href")
	val href: String? = null
)

data class Actor(

	@field:SerializedName("account_id")
	val accountId: String? = null,

	@field:SerializedName("nickname")
	val nickname: String? = null,

	@field:SerializedName("links")
	val links: Links? = null,

	@field:SerializedName("display_name")
	val displayName: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("uuid")
	val uuid: String? = null
)

data class Changes(

	@field:SerializedName("new")
	val newCommitData: CommitData? = null,

	@field:SerializedName("created")
	val created: Boolean? = null,

	@field:SerializedName("forced")
	val forced: Boolean? = null,

	@field:SerializedName("old")
	val oldCommitData: CommitData? = null,

	@field:SerializedName("closed")
	val closed: Boolean? = null,

	@field:SerializedName("commits")
	val commits: List<CommitsItem>? = null,

	@field:SerializedName("truncated")
	val truncated: Boolean? = null,

	@field:SerializedName("links")
	val links: Links? = null
)

data class Properties(
	val any: Any? = null
)

data class Repository(

	@field:SerializedName("is_private")
	val isPrivate: Boolean? = null,

	@field:SerializedName("owner")
	val owner: Owner? = null,

	@field:SerializedName("website")
	val website: String? = null,

	@field:SerializedName("workspace")
	val workspace: Workspace? = null,

	@field:SerializedName("full_name")
	val fullName: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("project")
	val project: Project? = null,

	@field:SerializedName("links")
	val links: Links? = null,

	@field:SerializedName("scm")
	val scm: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("uuid")
	val uuid: String? = null
)

data class User(

	@field:SerializedName("account_id")
	val accountId: String? = null,

	@field:SerializedName("nickname")
	val nickname: String? = null,

	@field:SerializedName("links")
	val links: Links? = null,

	@field:SerializedName("display_name")
	val displayName: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("uuid")
	val uuid: String? = null
)

data class Push(

	@field:SerializedName("changes")
	val changes: List<Changes>? = null
)

data class Html(

	@field:SerializedName("href")
	val href: String? = null
)

data class Summary(

	@field:SerializedName("markup")
	val markup: String? = null,

	@field:SerializedName("raw")
	val raw: String? = null,

	@field:SerializedName("html")
	val html: String? = null,

	@field:SerializedName("type")
	val type: String? = null
)

data class Comments(

	@field:SerializedName("href")
	val href: String? = null
)
