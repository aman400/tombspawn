package com.ramukaka.git

import com.ramukaka.extensions.authenticate
import com.ramukaka.models.config.App
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

/**
 * Client to execute git commands in local
 */
class GitClient {
    companion object {
        private val logger = LoggerFactory.getLogger("com.ramukaka.git.GitClient")
        fun clone(app: App, provider: CredentialProvider) {
            if(!try {
                    println("${app.name} generating")
                    initRepository(app).use {
                        it.objectDatabase.exists()
                    }
                } catch (exception: Exception) {
                    logger.error("Repository not found", exception)
                    false
                }) {
                val directory = File(app.dir!!).apply {
                    if(!this.exists()) {
                        if(this.mkdirs()) {
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
        fun initRepository(app: App): Repository {
            println("checking ${app.name}")

            val repositoryBuilder = FileRepositoryBuilder()
            repositoryBuilder.isMustExist = true
            repositoryBuilder.findGitDir()
            repositoryBuilder.readEnvironment()
            repositoryBuilder.gitDir = File("${app.dir}", ".git")
            return repositoryBuilder.build()
        }
    }
}