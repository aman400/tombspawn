package com.tombspawn.data

import com.tombspawn.data.cache.models.ApkCache
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock

class TestCachingService {
    lateinit var mockCachingService: CachingService
    @Before
    fun setup() {
        mockCachingService = mock(CachingService::class.java)
    }

    @Test
    fun cachingService() {
        Mockito.`when`(mockCachingService.getApkCache("abc", "master")).thenReturn(mutableListOf(
            ApkCache(mapOf("BRANCH" to "master"), "/")
        ))

        Assert.assertEquals(mockCachingService.getApkCache("abc", "master").first().pathOnDisk, "/")
    }
}