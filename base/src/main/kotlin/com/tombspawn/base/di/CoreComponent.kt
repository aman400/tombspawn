package com.tombspawn.base.di

import com.google.gson.Gson
import dagger.Component
import io.ktor.client.features.json.GsonSerializer

@Component(modules = [CoreModule::class])
interface CoreComponent {

    fun gson(): Gson

    fun gsonSerializer(): GsonSerializer

    @Component.Factory
    interface Factory {
        fun create(): CoreComponent
    }
}