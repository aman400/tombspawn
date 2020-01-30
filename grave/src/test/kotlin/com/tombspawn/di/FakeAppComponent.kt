package com.tombspawn.di

import com.tombspawn.base.di.scopes.AppScope
import dagger.Component

@Component(modules = [FakeDockerModule::class, FakeDataModule::class, FakeAppModule::class])
@AppScope
interface FakeAppComponent: AppComponent {
    @Component.Builder
    interface Builder {
        fun build(): FakeAppComponent
    }
}