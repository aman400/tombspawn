package com.tombspawn.skeleton.di

import com.tombspawn.base.common.Command
import com.tombspawn.skeleton.commandline.getCommandExecutor
import com.tombspawn.skeleton.di.qualifiers.AppDir
import com.tombspawn.skeleton.di.qualifiers.CloneDir
import com.tombspawn.skeleton.di.qualifiers.GradlePath
import com.tombspawn.skeleton.models.App
import com.tombspawn.skeleton.models.config.CommonConfig
import dagger.Module
import dagger.Provides
import io.ktor.application.Application
import kotlinx.coroutines.channels.SendChannel
import org.slf4j.LoggerFactory

@Module
class CommandLineModule {

    @GradlePath
    @Provides
    fun provideGradlePath(commonConfig: CommonConfig): String {
        return commonConfig.gradlePath ?: "./gradlew --no-daemon"
    }

    @AppDir
    @Provides
    fun provideAppDir(app: App): String {
        return app.appDir ?: app.cloneDir ?: "/"
    }

    @CloneDir
    @Provides
    fun provideCloneDir(app: App): String {
        return app.cloneDir ?: app.appDir ?: "/"
    }

    @Provides
    fun provideSendChannel(application: Application): SendChannel<Command> {
        return application.getCommandExecutor()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger("com.tombspawn.skeleton.di.CommandLineModule")
    }
}