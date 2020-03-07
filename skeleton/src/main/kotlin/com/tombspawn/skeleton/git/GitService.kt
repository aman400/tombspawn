package com.tombspawn.skeleton.git

import com.tombspawn.skeleton.models.App
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.FetchResult
import org.slf4j.LoggerFactory
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject
import kotlin.concurrent.read
import kotlin.concurrent.write

class GitService @Inject constructor(private val app: App, private val gitClient: GitClient) {
    private val LOGGER = LoggerFactory.getLogger("com.tombspawn.skeleton.git.GitService")
    private val repoLock = ReentrantReadWriteLock()

    suspend fun clone(): Boolean {
        LOGGER.trace("Write lock for clone")
        return repoLock.write {
            gitClient.clone(app.id, app.dir!!, app.uri!!)
        }
    }

    suspend fun fetchLogs(): RevCommit? {
        LOGGER.trace("Read Lock for fetching local logs")
        return repoLock.read {
            gitClient.fetchLogs(app.dir!!).await()
        }
    }

    suspend fun fetchRemoteBranches(): FetchResult {
        LOGGER.trace("Read Lock for fetching remote branches")
        return repoLock.read {
            gitClient.fetchRemoteAsync(app.dir!!).await()
        }
    }

    suspend fun getBranches(): List<String> {
        LOGGER.trace("Read lock to fetch branches")
        return repoLock.read {
            gitClient.getBranchesAsync(app.dir!!).await()
        }
    }

    suspend fun getTags(): List<String> {
        LOGGER.trace("Read lock to fetch tags")
        return repoLock.read {
            gitClient.getTagsAsync(app.dir!!).await()
        }
    }

    suspend fun checkout(branch: String): Boolean {
        LOGGER.trace("Write lock for checkout")
        return repoLock.write {
            gitClient.checkoutAsync(branch, app.dir!!).await()
        }
    }

    suspend fun pullCode(branch: String): Boolean {
        LOGGER.trace("Write lock to pull latest code")
        return repoLock.write {
            gitClient.pullLatestCodeAsync(branch, app.dir!!).await()
        }
    }

    suspend fun resetBranch(): Ref {
        LOGGER.trace("Write lock to reset branch")
        return repoLock.write {
            gitClient.resetBranch(app.dir!!).await()
        }
    }

    suspend fun clean(): MutableSet<String> {
        LOGGER.trace("Write lock to clean repo")
        return repoLock.write {
            gitClient.clean(app.dir!!).await()
        }
    }

    suspend fun stashCode(): RevCommit? {
        LOGGER.trace("Write lock to stash")
        return repoLock.write {
            gitClient.stashCode(app.dir!!).await()
        }
    }

    suspend fun clearStash(): ObjectId? {
        LOGGER.trace("Read lock to clear stash")
        return repoLock.read {
            gitClient.clearStash(app.dir!!).await()
        }
    }
}