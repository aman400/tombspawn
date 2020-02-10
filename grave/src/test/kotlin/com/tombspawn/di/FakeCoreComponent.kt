package com.tombspawn.di

import com.google.gson.Gson
import com.tombspawn.TestApplicationService
import dagger.Component

@Component(modules = [FakeCoreModule::class])
interface FakeCoreComponent {
    fun provideGson(): Gson

    @Component.Factory
    interface Factory {
        fun create(): FakeCoreComponent
    }
}