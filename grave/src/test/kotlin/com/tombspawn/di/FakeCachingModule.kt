package com.tombspawn.di

import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.data.StringMap
import com.tombspawn.di.qualifiers.ApkCacheMap
import com.tombspawn.di.qualifiers.AppCacheMap
import dagger.Module
import dagger.Provides
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.concurrent.TimeUnit

@Module
class FakeCachingModule {
    @Provides
    @AppCacheMap
    @AppScope
    fun provideAppCacheModule(): StringMap {
        val appCacheMap = Mockito.mock(StringMap::class.java)
        val appMap = mutableMapOf<String, String>()

        Mockito.`when`(
            appCacheMap.setData(
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.nullable(Long::class.java),
                ArgumentMatchers.nullable(TimeUnit::class.java)
            )
        ).then {
            appMap.put(it.arguments[0] as String, it.arguments[1] as String)
        }
        Mockito.`when`(appCacheMap.getData(ArgumentMatchers.anyString())).then { invocation ->
            invocation?.arguments?.firstOrNull()?.let {
                appMap[it]
            }
        }
        Mockito.`when`(appCacheMap.deleteKey(ArgumentMatchers.anyString())).then { invocation ->
            invocation?.arguments?.firstOrNull()?.let {
                appMap.remove(it)
                Unit
            }
        }
        Mockito.`when`(appCacheMap.close()).then {
            appMap.clear()
        }

        return appCacheMap
    }

    @Provides
    @ApkCacheMap
    @AppScope
    fun provideApkCacheMap(): StringMap {
        val appCacheMap = Mockito.mock(StringMap::class.java)
        val appMap = mutableMapOf<String, String>()

        Mockito.`when`(
            appCacheMap.setData(
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.nullable(Long::class.java),
                ArgumentMatchers.nullable(TimeUnit::class.java)
            )
        ).then {
            appMap.put(it.arguments[0] as String, it.arguments[1] as String)
        }
        Mockito.`when`(appCacheMap.getData(ArgumentMatchers.anyString())).then { invocation ->
            invocation?.arguments?.firstOrNull()?.let {
                appMap[it]
            }
        }
        Mockito.`when`(appCacheMap.deleteKey(ArgumentMatchers.anyString())).then { invocation ->
            invocation?.arguments?.firstOrNull()?.let {
                appMap.remove(it)
                Unit
            }
        }
        Mockito.`when`(appCacheMap.close()).then {
            appMap.clear()
        }

        return appCacheMap
    }
}