package com.tombspawn.skeleton.di

import com.tombspawn.base.common.CommandResponse
import com.tombspawn.skeleton.extensions.commandExecutor
import com.tombspawn.skeleton.git.CredentialProvider
import com.tombspawn.skeleton.gradle.GradleExecutor
import com.tombspawn.skeleton.models.App
import com.tombspawn.skeleton.models.config.CommonConfig
import io.ktor.application.Application
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

@UseExperimental(KtorExperimentalAPI::class)
val config = module {
    factory { (application: Application, coroutineScope: CoroutineScope) ->
        application.environment.config.configList("conf.apps").map {
            val responseListener = mutableMapOf<String, CompletableDeferred<CommandResponse>>()
            val requestExecutor = coroutineScope.commandExecutor(responseListener)
            val directory = it.propertyOrNull("dir")?.getString()
            App(
                it.property("id").getString(),
                it.propertyOrNull("name")?.getString(),
                it.propertyOrNull("app_url")?.getString(),
                it.propertyOrNull("repo_id")?.getString(),
                directory,
                it.property("remote_uri").getString(),
                get {
                    parametersOf(application, directory, responseListener, requestExecutor)
                } as GradleExecutor
            )
        }
    }

    single { (application: Application) ->
        application.environment.config.config("conf.common").let {
            CommonConfig(
                it.property("gradle_path").getString()
            )
        }
    }

    single { (application: Application) ->
        application.environment.config.config("conf.git").let {
            CredentialProvider(
                it.propertyOrNull("ssh_file")?.getString(), it.propertyOrNull("passphrase")?.getString(),
                it.propertyOrNull("username")?.getString(), it.propertyOrNull("password")?.getString()
            )
        }
    }
}