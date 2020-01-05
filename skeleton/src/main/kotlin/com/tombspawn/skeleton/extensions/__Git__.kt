@file:JvmName("GitUtils")

package com.tombspawn.skeleton.extensions

import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.RefAlreadyExistsException
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger("com.tombspawn.skeleton.extensions.GitUtils")

fun Git.checkout(branch: String, start: String?) {
    try {
        checkout()
            .setName(branch)
            .setCreateBranch(true)
            .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
            .setStartPoint(start)
            .call()
        LOGGER.debug("Checked out to branch: $branch")
    } catch (exception: RefAlreadyExistsException) {
        checkout()
            .setName(branch)
            .setCreateBranch(false)
            .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
            .call()
        LOGGER.debug("Checked out to branch: $branch")
    }
}