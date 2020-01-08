package com.tombspawn.di

import com.tombspawn.ApplicationService
import com.tombspawn.base.di.CoreComponent
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.di.qualifiers.SlackHttpClient
import dagger.BindsInstance
import dagger.Component
import io.ktor.application.Application
import io.ktor.client.HttpClient

@Component(modules = [AppModule::class, DockerModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent {

    fun applicationService(): ApplicationService

    @Component.Builder
    interface Builder {
        fun build(): AppComponent
        fun plus(coreComponent: CoreComponent): Builder
        @BindsInstance fun plus(application: Application): Builder
    }
}