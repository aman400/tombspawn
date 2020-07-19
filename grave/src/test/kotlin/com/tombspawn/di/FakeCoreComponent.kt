package com.tombspawn.di

import com.google.gson.Gson
import com.tombspawn.git.CredentialProvider
import dagger.Component

@Component(modules = [FakeCoreModule::class, FakeAppModule::class])
interface FakeCoreComponent {
    fun provideGson(): Gson

    fun provideCredentialProvider(): CredentialProvider

    @Component.Factory
    interface Factory {
        fun create(fakeCoreModule: FakeCoreModule,
                   fakeAppModule: FakeAppModule
        ): FakeCoreComponent
    }
}