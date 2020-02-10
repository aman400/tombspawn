package com.tombspawn.di

import com.tombspawn.Grave
import com.tombspawn.base.di.CoreComponent
import com.tombspawn.base.di.scopes.AppScope
import dagger.BindsInstance
import dagger.Component
import io.ktor.application.Application

@Component(
    modules = [AppModule::class, DockerModule::class, CachingModule::class],
    dependencies = [CoreComponent::class]
)
@AppScope
interface AppComponent {

    fun inject(grave: Grave)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application, coreComponent: CoreComponent): AppComponent
    }
}