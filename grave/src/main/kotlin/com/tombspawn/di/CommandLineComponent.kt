package com.tombspawn.di

import com.tombspawn.base.di.scopes.CoroutineScope
import dagger.BindsInstance
import dagger.Component

@CoroutineScope
@Component(modules = [CommandLineModule::class])
interface CommandLineComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance coroutineScope: kotlinx.coroutines.CoroutineScope): CommandLineComponent
    }
}