package com.tombspawn.skeleton.git

import com.tombspawn.skeleton.extensions.authenticate
import com.tombspawn.skeleton.models.App
import kotlinx.coroutines.*
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.ProgressMonitor
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.FetchResult
import org.eclipse.jgit.transport.TagOpt
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume

class GitClient @Inject constructor(private val provider: CredentialProvider) {
    suspend fun clone(dir: String, gitUri: String) = suspendCancellableCoroutine<Boolean> { continuation ->
        if (!try {
                LOGGER.debug("Generating app")
                initRepository(dir).use {
                    it.objectDatabase.exists()
                }
            } catch (exception: Exception) {
                LOGGER.error("Repository not found", exception)
                false
            }
        ) {
            val directory = File(dir).apply {
                if (!this.exists()) {
                    if (this.mkdirs()) {
                        LOGGER.info("${this.absolutePath} created")
                    } else {
                        LOGGER.error("unable to create ${this.absolutePath}")
                    }
                } else {
                    LOGGER.info("$dir already exists")
                }
            }
            LOGGER.debug("Cloning app")
            Git.cloneRepository()
                .setURI(gitUri)
                .setDirectory(directory)
                .setProgressMonitor(object : ProgressMonitor {
                    override fun update(completed: Int) {
                        LOGGER.trace("Progress $completed%")
                    }

                    override fun start(totalTasks: Int) {
                        LOGGER.debug("Total task $totalTasks")
                    }

                    override fun beginTask(title: String?, totalWork: Int) {
                        LOGGER.debug("Begin task $title $totalWork")
                    }

                    override fun endTask() {
                        LOGGER.debug("End task")
                    }

                    override fun isCancelled(): Boolean {
                        return false
                    }
                })
                .setCloneAllBranches(true)
                .setCallback(object : CloneCommand.Callback {
                    override fun checkingOut(commit: AnyObjectId?, path: String?) {
                        LOGGER.info("Checking out $path $commit")
                    }

                    override fun initializedSubmodules(submodules: MutableCollection<String>?) {
                        LOGGER.info("Initialized Submodules")
                    }

                    override fun cloningSubmodule(path: String?) {
                        LOGGER.info("Cloning Submodules")
                    }

                })
                .authenticate(provider)
                .call()
            LOGGER.debug("Clone completed")
            continuation.resume(true)
        } else {
            runBlocking {
                delay(5000)
                continuation.resume(true)
            }
        }
    }

    suspend fun fetchRemoteAsync(dir: String): Deferred<FetchResult> = coroutineScope {
        async(Dispatchers.IO) {
            var origin: String
            return@async Git(initRepository(dir).also {
                val storedConfig = it.config
                origin = storedConfig.getSubsections("remote").first()
            }).use { git ->
                git.fetch().setRemote(origin)
                    .setRemoveDeletedRefs(true)
                    .setTagOpt(TagOpt.FETCH_TAGS)
                    .authenticate(provider).call()
            }
        }
    }

    suspend fun getBranchesAsync(dir: String): Deferred<List<String>> = coroutineScope {
        async(Dispatchers.IO) {
            var origin: String
            return@async Git(initRepository(dir).also {
                val storedConfig = it.config
                origin = storedConfig.getSubsections("remote").first()
            }).use { git ->
                return@use git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE)
                    .call()
                    .filter { ref ->
                        ref.name.contains(origin) && !ref.name.contains("HEAD", ignoreCase = true)
                    }.map {
                        // Strip off remote from branch name
                        it.name.substringAfter("refs/remotes/$origin/")
                    }
            }
        }
    }

    suspend fun getTagsAsync(dir: String): Deferred<List<String>> = coroutineScope {
        async(Dispatchers.IO) {
            return@async Git(initRepository(dir)).use { git ->
                var tags = git.tagList().call()
                RevWalk(git.repository).use { revWalk ->
                    tags = tags.sortedByDescending {
                        revWalk.parseCommit(it.objectId).commitTime
                    }
                }
                tags.map {
                    it.name.substringAfter("refs/tags/")
                }
            }
        }
    }

    suspend fun stashCode(dir: String): Deferred<RevCommit?> = coroutineScope {
        async(Dispatchers.IO) {
            try {
                return@async Git(initRepository(dir)).use { git ->
                    git.stashCreate().setIncludeUntracked(true)
                        .setWorkingDirectoryMessage("Stash files")
                        .setIndexMessage("Stash: ${System.currentTimeMillis()}")
                        .call()
                }
            } catch (exception: Exception) {
                LOGGER.error("Unable to stash code", exception)
                return@async null
            }
        }
    }

    suspend fun clearStash(dir: String): Deferred<ObjectId?> = coroutineScope {
        async(Dispatchers.IO) {
            try {
                return@async Git(initRepository(dir)).use { git ->
                    git.stashDrop().setAll(true).call()
                }
            } catch (exception: Exception) {
                LOGGER.error("Unable to clear stash", exception)
                return@async null
            }
        }
    }

    suspend fun checkoutAsync(branch: String, dir: String): Deferred<Boolean> = coroutineScope {
        async(Dispatchers.IO) {
            return@async Git(initRepository(dir)).use { git ->
                git.branchList().call().filter {
                    it.name.substringAfter("refs/heads/") == branch
                }.map {
                    it.name.substringAfter("refs/heads/")
                }.firstOrNull()?.let { localBranch: String ->
                    git.checkout().setName(localBranch).call()
                    if (git.pull().setRemoteBranchName(localBranch)
                            .authenticate(provider).call().isSuccessful) {
                        LOGGER.info("Pulled latest code")
                    } else {
                        LOGGER.warn("Unable to pull code")
                    }
                    true
                } ?: false
            }
        }
    }

    private fun initRepository(dir: String): Repository {
        LOGGER.debug("Verifying Directory $dir")

        val repositoryBuilder = FileRepositoryBuilder()
        repositoryBuilder.isMustExist = true
        repositoryBuilder.findGitDir()
        repositoryBuilder.readEnvironment()
        repositoryBuilder.gitDir = File(dir, ".git")
        return repositoryBuilder.build()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger("com.tombspawn.skeleton.git.GitClient")
    }
}