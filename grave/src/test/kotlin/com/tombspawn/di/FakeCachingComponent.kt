package com.tombspawn.di

import com.google.gson.Gson
import com.tombspawn.TestApplicationService
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.data.TestCachingService
import dagger.Component

@Component(modules = [FakeCachingModule::class],
    dependencies = [FakeCoreComponent::class])
@AppScope
interface FakeCachingComponent {
    fun inject(testCachingService: TestCachingService)
    fun inject(testApplicationService: TestApplicationService)

    fun provideGson(): Gson

    @Component.Factory
    interface Factory {
        fun create(fakeCoreComponent: FakeCoreComponent): FakeCachingComponent
    }
}