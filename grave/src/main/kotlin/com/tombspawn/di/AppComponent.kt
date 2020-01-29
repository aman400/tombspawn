package com.tombspawn.di

import com.tombspawn.ApplicationService
import com.tombspawn.base.di.CoreComponent
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.data.CachingService
import com.tombspawn.di.qualifiers.SlackHttpClient
import dagger.BindsInstance
import dagger.Component
import io.ktor.application.Application
import io.ktor.client.HttpClient

@Component(
    modules = [AppModule::class, DockerModule::class, CachingModule::class],
    dependencies = [CoreComponent::class]
)
@AppScope
interface AppComponent {

    fun applicationService(): ApplicationService

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application, coreComponent: CoreComponent): AppComponent
    }
}