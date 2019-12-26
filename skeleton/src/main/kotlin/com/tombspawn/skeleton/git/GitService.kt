package com.tombspawn.skeleton.git

import com.tombspawn.skeleton.models.App
import kotlinx.coroutines.Deferred
import org.eclipse.jgit.transport.FetchResult
import javax.inject.Inject

class GitService @Inject constructor(private val app: App, private val gitClient: GitClient) {

    fun clone(onComplete: ((success: Boolean) -> Unit)? = null) {
        gitClient.clone(app.dir!!, app.uri!!, onComplete)
    }

    suspend fun fetchRemoteBranches(): Deferred<FetchResult> {
        return gitClient.fetchRemoteAsync(app.dir!!)
    }

    suspend fun getBranches(): Deferred<List<String>> {
        return gitClient.getBranchesAsync(app.dir!!)
    }

    suspend fun getTags(): Deferred<List<String>> {
        return gitClient.getTagsAsync(app.dir!!)
    }

    suspend fun checkout(branch: String): Deferred<Boolean> {
        return gitClient.checkoutAsync(branch, app.dir!!)
    }
}