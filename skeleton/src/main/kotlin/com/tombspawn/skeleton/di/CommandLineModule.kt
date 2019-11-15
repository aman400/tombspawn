package com.tombspawn.skeleton.di

import com.tombspawn.base.common.Command
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.skeleton.commandline.getCommandExecutor
import com.tombspawn.skeleton.di.qualifiers.AppDir
import com.tombspawn.skeleton.di.qualifiers.GradlePath
import com.tombspawn.skeleton.git.CredentialProvider
import com.tombspawn.skeleton.gradle.GradleExecutor
import com.tombspawn.skeleton.models.App
import com.tombspawn.skeleton.models.config.CommonConfig
import dagger.Module
import dagger.Provides
import io.ktor.application.Application
import kotlinx.coroutines.channels.SendChannel

@Module
class CommandLineModule {

    @GradlePath
    @Provides
    fun provideGradlePath(commonConfig: CommonConfig): String {
        return commonConfig.gradlePath
    }

    @AppDir
    @Provides
    fun provideAppDir(app: App): String {
        return app.dir ?: "/"
    }

    @Provides
    fun provideSendChannel(application: Application): SendChannel<Command> {
        return application.getCommandExecutor()
    }
}