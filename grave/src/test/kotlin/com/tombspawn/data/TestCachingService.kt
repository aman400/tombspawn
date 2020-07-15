package com.tombspawn.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tombspawn.common.getResourceFile
import com.tombspawn.data.cache.models.ApkCache
import com.tombspawn.di.DaggerFakeCachingComponent
import com.tombspawn.di.DaggerFakeCoreComponent
import com.tombspawn.di.FakeAppModule
import com.tombspawn.di.FakeCoreModule
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

class TestCachingService {
    @Inject
    lateinit var cachingService: CachingService

    @Inject
    lateinit var gson: Gson

    @Before
    fun setup() {
        DaggerFakeCachingComponent
            .factory()
            .create(DaggerFakeCoreComponent.factory().create(FakeCoreModule(), FakeAppModule()))
            .inject(this)
        createCache()
    }

    private fun createCache() {
        val rawCache1 = javaClass.getResourceFile("com/tombspawn/data/apk_cache_1.json")
            .bufferedReader(Charsets.UTF_8).readText()
        val type = object : TypeToken<List<ApkCache>>() {}.type
        val cache1 = gson.fromJson<List<ApkCache>>(rawCache1, type)
        cache1.forEach {
            cachingService.cacheApk("fleet", "locus-floater-fe", it)
        }

        val rawCache2 = javaClass.getResourceFile("com/tombspawn/data/apk_cache_2.json")
            .bufferedReader(Charsets.UTF_8).readText()
        val cache2 = gson.fromJson<List<ApkCache>>(rawCache2, type)
        cache2.forEach {
            cachingService.cacheApk("consumer", "master", it)
        }

        val rawCache3 = javaClass.getResourceFile("com/tombspawn/data/apk_cache_3.json")
            .bufferedReader(Charsets.UTF_8).readText()
        val cache3 = gson.fromJson<List<ApkCache>>(rawCache3, type)
        cache3.forEach {
            cachingService.cacheApk("fleet", "fota-bypass-change", it)
        }

        val rawCache4 = javaClass.getResourceFile("com/tombspawn/data/apk_cache_4.json")
            .bufferedReader(Charsets.UTF_8).readText()
        val cache4 = gson.fromJson<List<ApkCache>>(rawCache4, type)
        cache4.forEach {
            cachingService.cacheApk("consumer", "development", it)
        }
    }

    @Test
    fun `cached file available test`() {
        val cacheToFind = ApkCache(
            mapOf(
                "BRANCH" to "development",
                "BUILD_TYPE" to "debug",
                "APP_URL" to "https://dileep-con.api.tech/api/v1/"
            )
        )
        Assert.assertEquals("Failed to find cache", "/app/temp/19959656946720826/app-debug.apk",
            cachingService.getApkCache("consumer", "development").firstOrNull {
                it == cacheToFind
            }?.pathOnDisk
        )

        val cacheToFindConsumerMaster = ApkCache(
            mapOf(
                "BRANCH" to "master",
                "BUILD_TYPE" to "release",
                "APP_URL" to "https://dev-api-con-test.tech/api/v1/"
            )
        )
        Assert.assertEquals("Failed to find master consumer apk", "/app/temp/19867581640597275/app-release.apk",
            cachingService.getApkCache("consumer", "master").firstOrNull {
                it == cacheToFindConsumerMaster
            }?.pathOnDisk
        )
    }

    @Test
    fun `cached file not available test`() {
        val cacheToFind = ApkCache(
            mapOf(
                "BRANCH" to "master",
                "BUILD_TYPE" to "debug",
                "APP_URL" to "https://dileep-con.api.tech/api/v1/"
            )
        )
        Assert.assertNull("Failed to find cache",
            cachingService.getApkCache("consumer", "development").firstOrNull {
                it == cacheToFind
            }?.pathOnDisk
        )

        val cacheToFindConsumerMaster = ApkCache(
            mapOf(
                "BRANCH" to "master",
                "BUILD_TYPE" to "release",
                "APP_URL" to "https://stage-con.api.tech/api/v1/"
            )
        )
        Assert.assertNull("Failed to find master consumer apk",
            cachingService.getApkCache("consumer", "master").firstOrNull {
                it == cacheToFindConsumerMaster
            }?.pathOnDisk
        )
    }
}