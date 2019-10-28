package com.tombspawn.skeleton.git

import com.tombspawn.skeleton.extensions.authenticate
import com.tombspawn.skeleton.models.App
import com.typesafe.config.ConfigBeanFactory
import com.typesafe.config.ConfigFactory
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.ProgressMonitor
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.util.FS
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

class GitClient(private val app: App, private val provider: CredentialProvider) {
    private val logger = LoggerFactory.getLogger("GitClient")
    fun clone() {
        if (!try {
                println("${app.name} generating")
                initRepository().use {
                    it.objectDatabase.exists()
                }
            } catch (exception: Exception) {
                logger.error("Repository not found", exception)
                false
            }
        ) {
            val directory = File(app.dir!!).apply {
                if (!this.exists()) {
                    if (this.mkdirs()) {
                        println("${this.absolutePath} created")
                    } else {
                        println("unable to create ${this.absolutePath}")
                    }
                } else {
                    println("${app.dir} already exists")
                }
            }
            println("Cloning ${app.name}")
            Git.cloneRepository()
                .setURI(app.uri)
                .setDirectory(directory)
                .setProgressMonitor(object: ProgressMonitor {
                    override fun update(completed: Int) {
                        LOGGER.debug("Progress $completed%")
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
                .setCallback(object: CloneCommand.Callback {
                    override fun checkingOut(commit: AnyObjectId?, path: String?) {
                        println("Checking out $path $commit")
                    }

                    override fun initializedSubmodules(submodules: MutableCollection<String>?) {
                        println("Initialized Submodules")
                    }

                    override fun cloningSubmodule(path: String?) {
                        println("Cloning Submodules")
                    }

                })
                .authenticate(provider)
                .call()
        }
    }

    @Throws(IOException::class)
    fun initRepository(): Repository {
        println("checking ${app.name}")

        val repositoryBuilder = FileRepositoryBuilder()
        repositoryBuilder.isMustExist = true
        repositoryBuilder.findGitDir()
        repositoryBuilder.readEnvironment()
        repositoryBuilder.gitDir = File("${app.dir}", ".git")
        return repositoryBuilder.build()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger("GitClient")
    }
}