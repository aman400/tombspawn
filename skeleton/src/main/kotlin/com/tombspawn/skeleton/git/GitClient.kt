package com.tombspawn.skeleton.git

import com.google.common.annotations.VisibleForTesting
import com.tombspawn.base.extensions.moveToDirectory
import com.tombspawn.skeleton.extensions.authenticate
import com.tombspawn.skeleton.extensions.checkout
import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.lib.*
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.FetchResult
import org.eclipse.jgit.transport.TagOpt
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume

class GitClient @Inject constructor(private val provider: CredentialProvider) {
    suspend fun clone(appId: String, dir: String, gitUri: String) = suspendCancellableCoroutine<Boolean> { continuation ->
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
                    LOGGER.info("Moving all files in $dir to temp directory")
                    val files = this.listFiles()
                    val tempDir = File(this@apply.parent, "temp_$appId")
                    files?.filter {
                        it?.exists() == true
                    }?.also {
                        if(!tempDir.exists()) {
                            tempDir.mkdirs()
                            LOGGER.info("Creating temp directory")
                        } else {
                            LOGGER.info("Temp directory already exists")
                        }
                    }?.forEach {
                        try {
                            LOGGER.info("Moving ${it.absolutePath} to ${tempDir.absolutePath}")
                            it.moveToDirectory(tempDir)
                        } catch (exception: Exception) {
                            LOGGER.error("Unable to move file ${it.absolutePath} to temp directory", exception)
                        }
                    }
                    LOGGER.info("Moved all files to temp directory")
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

            try {
                LOGGER.info("Moving files back to original directory")
                File(directory.parentFile, "temp_$appId").apply {
                    if (this.exists()) {
                        val files = this.listFiles()
                        files?.filter {
                            it?.exists() == true
                        }?.forEach {
                            try {
                                LOGGER.info("Moving ${it.absolutePath} to ${directory.absolutePath}")
                                it.moveToDirectory(directory)
                            } catch (exception: Exception) {
                                LOGGER.error("Unable to move file ${it.absolutePath} back to original directory", exception)
                            }
                        }
                        this.deleteRecursively()
                    } else {
                        LOGGER.info("No files to be moved to original directory")
                    }
                }
                LOGGER.info("Moved files back to original directory")
            } catch (exception: Exception) {
                LOGGER.error("Unable to move files back to directory", exception)
            }
            continuation.resume(true)
        } else {
            runBlocking {
                delay(5000)
                continuation.resume(true)
            }
        }
    }

    suspend fun fetchLogs(dir: String): Deferred<RevCommit?> = coroutineScope {
        async(Dispatchers.IO) {
            return@async Git(initRepository(dir)).use {git ->
                git.log().setMaxCount(1).call().firstOrNull()
            }
        }
    }

    suspend fun fetchRemoteAsync(dir: String): Deferred<FetchResult> = coroutineScope {
        async(Dispatchers.IO) {
            var origin: String
            return@async Git(initRepository(dir).also {
                val storedConfig = it.config
                origin = storedConfig.getSubsections("remote").firstOrNull() ?: "origin"
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
                origin = storedConfig.getSubsections("remote").firstOrNull() ?: "origin"
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

    suspend fun clean(dir: String): Deferred<MutableSet<String>> = coroutineScope {
        async(Dispatchers.IO) {
            return@async Git(initRepository(dir)).use { git ->
                git.clean().setCleanDirectories(true)
                    .setForce(true).call()
            }
        }
    }

    suspend fun resetBranch(dir: String): Deferred<Ref> = coroutineScope {
        async(Dispatchers.IO) {
            return@async Git(initRepository(dir)).use { git ->
                git.reset().setMode(ResetCommand.ResetType.HARD)
                    .setRef(Constants.HEAD)
                    .call()
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

    suspend fun checkoutAsync(ref: String, dir: String): Deferred<Boolean> = coroutineScope {
        async {
            var config: StoredConfig
            return@async Git(initRepository(dir).also {
                config = it.config
            }).use { git ->
                val remote = config.getSubsections("remote").firstOrNull() ?: "origin"
                val localBranch = git.branchList().call().filter {
                    it.name.substringAfter("refs/heads/") == ref
                }.map {
                    it.name.substringAfter("refs/heads/")
                }.firstOrNull()
                if (localBranch != null) {
                    git.checkout().setName(localBranch).call()
                    LOGGER.info("Checked out to branch $ref")
                    pullLatestCodeAsync(ref, dir).await()
                } else {
                    val localTag = git.tagList().call().firstOrNull { tag ->
                        tag.name.endsWith(ref, true)
                    }
                    if(localTag != null) {
                        try {
                            git.checkout().setCreateBranch(false).setName(ref)
                                .setStartPoint(localTag.name).call()
                            LOGGER.info("Checked out to tag $ref")
                            true
                        } catch (exception: Exception) {
                            LOGGER.error("Unable to checkout to tag $ref", exception)
                            false
                        }
                    } else {
                        val remoteBranch =
                            git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call().filter {
                                it.name.substringAfter("refs/remotes/$remote/") == ref
                            }.map {
                                it.name.substringAfter("refs/remotes/$remote/")
                            }.firstOrNull()
                        if (remoteBranch != null) {
                            LOGGER.info("Checking out to remote branch $ref")
                            fetchAndCheckoutRemoteBranch(git, remote, ref)
                        } else {
                            false
                        }
                    }
                }
            }
        }
    }

    private fun fetchAndCheckoutRemoteBranch(git: Git, remote: String, branch: String): Boolean {
        return git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call().filter {
            it.name.substringAfter("refs/remotes/$remote/") == branch
        }.map {
            it.name.substringAfter("refs/remotes/$remote/")
        }.firstOrNull()?.let { localBranch: String ->
            git.checkout(localBranch, "$remote/$localBranch")
            true
        } ?: false
    }

    suspend fun pullLatestCodeAsync(branch: String, dir: String): Deferred<Boolean> = coroutineScope {
        async {
            return@async Git(initRepository(dir)).use {git ->
                if (git.pull().setRemoteBranchName(branch)
                        .authenticate(provider).call().isSuccessful
                ) {
                    LOGGER.info("Pulled latest code")
                    true
                } else {
                    LOGGER.warn("Unable to pull code")
                    false
                }
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