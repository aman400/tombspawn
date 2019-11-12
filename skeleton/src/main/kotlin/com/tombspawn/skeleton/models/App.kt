package com.tombspawn.skeleton.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tombspawn.skeleton.git.GitClient
import com.tombspawn.skeleton.gradle.GradleExecutor
import kotlinx.coroutines.Deferred
import org.eclipse.jgit.transport.FetchResult

data class App constructor(
    @SerializedName("id")
    var id: String,
    @SerializedName("name")
    var name: String?,
    @SerializedName("app_url")
    var appUrl: String?,
    @SerializedName("repo_id")
    var repoId: String?,
    @SerializedName("dir")
    var dir: String? = null,
    @SerializedName("remote_uri")
    var uri: String? = null
) {
    var gradleExecutor: GradleExecutor? = null
    var gitClient: GitClient? = null

    fun clone() {
        if(!this.dir.isNullOrBlank() && !this.uri.isNullOrBlank()) {
            gitClient?.clone(this.dir!!, this.uri!!)
        }
    }

    suspend fun fetchRemotesAsync(): Deferred<FetchResult>? {
        return this.dir?.let {
            gitClient?.fetchRemoteAsync(it)
        }
    }

    suspend fun getBranchesAsync(): Deferred<List<String>>? {
        return this.dir?.let {
            gitClient?.getBranchesAsync(it)
        }
    }

    suspend fun getTagsAsync(): Deferred<List<String>>? {
        return this.dir?.let {
            gitClient?.getTagsAsync(it)
        }
    }

    suspend fun checkoutAsync(branch: String): Deferred<Boolean>? {
        return this.dir?.let {
            gitClient?.checkoutAync(branch, it)
        }
    }
}