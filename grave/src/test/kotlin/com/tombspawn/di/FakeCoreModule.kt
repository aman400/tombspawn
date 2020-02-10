package com.tombspawn.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides

@Module
class FakeCoreModule {

    @Provides
    fun providesGson(): Gson {
        return Gson()
    }
}