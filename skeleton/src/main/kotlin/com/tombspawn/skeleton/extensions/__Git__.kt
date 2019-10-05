package com.tombspawn.skeleton.extensions

import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.RefAlreadyExistsException

fun Git.checkout(branch: String, start: String?) {
    try {
        checkout()
            .setName(branch)
            .setCreateBranch(true)
            .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
            .setStartPoint(start)
            .call()
        println("Checked out to branch: $branch")
    } catch (exception: RefAlreadyExistsException) {
        checkout()
            .setName(branch)
            .setCreateBranch(false)
            .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
            .call()
        println("Checked out to branch: $branch")
    }
}