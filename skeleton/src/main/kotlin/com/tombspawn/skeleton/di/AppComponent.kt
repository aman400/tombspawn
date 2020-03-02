package com.tombspawn.skeleton.di

import com.tombspawn.base.di.CoreComponent
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.skeleton.ApplicationService
import com.tombspawn.skeleton.Skeleton
import com.tombspawn.skeleton.di.qualifiers.UploadAppClient
import dagger.BindsInstance
import dagger.Component
import io.ktor.application.Application
import io.ktor.client.HttpClient

@Component(modules = [AppModule::class, CommandLineModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent {

    fun applicationService(): ApplicationService

    @UploadAppClient
    fun uploadHttpClient(): HttpClient

    fun inject(skeleton: Skeleton)

    @Component.Builder
    interface Builder {
        fun build(): AppComponent
        fun plus(coreComponent: CoreComponent): Builder
        @BindsInstance
        fun plus(application: Application): Builder
    }
}