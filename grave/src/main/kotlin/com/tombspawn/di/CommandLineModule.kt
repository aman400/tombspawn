package com.tombspawn.di

import com.tombspawn.app.CommandLineService
import com.tombspawn.base.common.Command
import com.tombspawn.base.common.CommandResponse
import com.tombspawn.base.di.scopes.CoroutineScope
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel

@Module
class CommandLineModule {

    @Provides
    @CoroutineScope
    fun provideCommandLineService(coroutineScope: kotlinx.coroutines.CoroutineScope): CommandLineService {
        return CommandLineService(coroutineScope)
    }

    @Provides
    @CoroutineScope
    fun provideCommandExecutor(commandLineService: CommandLineService, responseListeners: MutableMap<String, CompletableDeferred<CommandResponse>>): SendChannel<Command> {
        return commandLineService.commandExecutor(responseListeners)
    }

    @Provides
    @CoroutineScope
    fun responseListener(): MutableMap<String, CompletableDeferred<CommandResponse>> {
        return mutableMapOf()
    }
}