package com.tombspawn.di

import com.tombspawn.ApplicationService
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.data.CachingService
import dagger.Component

@Component(modules = [FakeDockerModule::class, FakeDataModule::class, FakeAppModule::class])
@AppScope
interface FakeAppComponent: AppComponent {
    fun cachingService(): CachingService
    override fun applicationService(): ApplicationService

    @Component.Builder
    interface Builder {
        fun build(): FakeAppComponent
    }
}