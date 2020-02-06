package com.tombspawn.di

import dagger.Component

@Component(modules = [FakeDockerModule::class])
interface FakeDockerComponent {

    @Component.Factory
    interface Factory {
        fun create(): FakeDockerComponent
    }
}