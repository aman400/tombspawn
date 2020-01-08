package com.tombspawn.skeleton.extensions

import com.tombspawn.skeleton.exception.CredentialsNotFoundException
import com.tombspawn.skeleton.git.CredentialProvider
import com.tombspawn.skeleton.git.SshTransportConfigCallback
import org.eclipse.jgit.api.GitCommand
import org.eclipse.jgit.api.TransportCommand
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider


@Throws(CredentialsNotFoundException::class)
fun <C : GitCommand<*>?, T> TransportCommand<C, T>.authenticate(credentialProvider: CredentialProvider): TransportCommand<C, T> {
    if (!credentialProvider.isPresent()) {
        throw CredentialsNotFoundException(credentialProvider.getErrorMessage())
    } else {

        if (!credentialProvider.username.isNullOrEmpty()) {
            setCredentialsProvider(
                UsernamePasswordCredentialsProvider(
                    credentialProvider.username,
                    credentialProvider.password ?: ""
                )
            )
        } else {
            setTransportConfigCallback(
                SshTransportConfigCallback(
                    credentialProvider.sshFilePath,
                    credentialProvider.passphrase
                )
            )
        }
    }

    return this
}