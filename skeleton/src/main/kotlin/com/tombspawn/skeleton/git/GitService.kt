package com.tombspawn.skeleton.git

import com.tombspawn.skeleton.models.App
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.FetchResult
import java.io.File
import javax.inject.Inject

class GitService @Inject constructor(private val app: App, private val gitClient: GitClient) {

    suspend fun clone(): Boolean {
        return gitClient.clone(app.dir!!, app.uri!!)
    }

    suspend fun fetchLogs(): Deferred<RevCommit?> {
        return gitClient.fetchLogs(app.dir!!)
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

    suspend fun pullCode(branch: String): Deferred<Boolean> {
        return gitClient.pullLatestCode(branch, app.dir!!)
    }

    suspend fun resetBranch(): Deferred<Ref> {
        return gitClient.resetBranch(app.dir!!)
    }

    suspend fun clean(): Deferred<MutableSet<String>> {
        return gitClient.clean(app.dir!!)
    }

    suspend fun stashCode(): Deferred<RevCommit?> {
        return gitClient.stashCode(app.dir!!)
    }

    suspend fun clearStash(): Deferred<ObjectId?> {
        return gitClient.clearStash(app.dir!!)
    }
}