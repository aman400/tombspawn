package com.tombspawn.skeleton.git

import com.tombspawn.skeleton.extensions.authenticate
import com.tombspawn.skeleton.models.App
import com.typesafe.config.ConfigBeanFactory
import com.typesafe.config.ConfigFactory
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
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
                }
            }
            println("Cloning ${app.name}")
            Git.cloneRepository()
                .setURI(app.uri)
                .setDirectory(directory)
                .setCloneAllBranches(true)
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
}