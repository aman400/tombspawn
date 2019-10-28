package com.tombspawn.skeleton.di

import com.tombspawn.base.common.Command
import com.tombspawn.base.common.CommandResponse
import com.tombspawn.base.config.JsonApplicationConfig
import com.tombspawn.skeleton.extensions.commandExecutor
import com.tombspawn.skeleton.git.CredentialProvider
import com.tombspawn.skeleton.git.GitClient
import com.tombspawn.skeleton.gradle.GradleExecutor
import com.tombspawn.skeleton.models.App
import com.tombspawn.skeleton.models.config.CommonConfig
import io.ktor.application.Application
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

@UseExperimental(KtorExperimentalAPI::class)
val config = module {
    factory { (application: Application, coroutineScope: CoroutineScope) ->
        val responseListener = mutableMapOf<String, CompletableDeferred<CommandResponse>>()
        val requestExecutor = coroutineScope.commandExecutor(responseListener)
        (application.environment.config as JsonApplicationConfig).property("app")
            .getAs(App::class.java).apply {
                this.gradleExecutor = get {
                    parametersOf(application, this.dir, responseListener, requestExecutor)
                } as GradleExecutor
            }
    }

    single { (application: Application) ->
        (application.environment.config as JsonApplicationConfig).property("common")
            .getAs(CommonConfig::class.java)
    }

    single { (application: Application) ->
        (application.environment.config as JsonApplicationConfig).property("git")
            .getAs(CredentialProvider::class.java)
    }

    factory { (app: App, credentialProvider: CredentialProvider) ->
        GitClient(app, credentialProvider)
    }
}

val gradleBotClient = module {
    factory { (application: Application, appDir: String, responseListener: MutableMap<String, CompletableDeferred<CommandResponse>>,
                  requestExecutor: SendChannel<Command>) ->
        GradleExecutor(
            appDir,
            get<CommonConfig> {
                parametersOf(application)
            }.gradlePath,
            responseListener,
            requestExecutor,
            get {
                parametersOf(application)
            }
        )
    }
}