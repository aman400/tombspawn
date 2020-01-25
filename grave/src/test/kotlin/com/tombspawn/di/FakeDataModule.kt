package com.tombspawn.di

import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.data.DatabaseService
import com.tombspawn.data.StringMap
import com.tombspawn.di.qualifiers.ApkCacheMap
import com.tombspawn.di.qualifiers.AppCacheMap
import dagger.Module
import dagger.Provides
import org.mockito.Mockito.mock

@Module
class FakeDataModule {

    @AppScope
    @AppCacheMap
    @Provides
    fun provideAppCacheMap(): StringMap {
        return mock(StringMap::class.java)
    }

    @AppScope
    @ApkCacheMap
    @Provides
    fun provideApkCacheMap(): StringMap {
        return mock(StringMap::class.java)
    }

    @AppScope
    @Provides
    fun provideDatabaseService(): DatabaseService {
        return mock(DatabaseService::class.java)
    }
}