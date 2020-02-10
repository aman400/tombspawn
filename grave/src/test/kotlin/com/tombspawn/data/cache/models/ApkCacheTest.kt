package com.tombspawn.data.cache.models

import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals

class ApkCacheTest {
    @Test
    fun equalsMethodTest() {
        val apkCache = ApkCache(mapOf("APP_URL" to "https://abc.com/a/b", "PROGUARD" to "true", "BRANCH" to "master",
            "commit_id" to "abcdefghijklmnop", "commit_message" to "A Commit message", "commit_author" to "Amandeep"),
            "/Users/app/abc/application.apk")
        val secondCache = ApkCache(mapOf("APP_URL" to "https://abc.com/a/b", "PROGUARD" to "true", "BRANCH" to "master",
            "commit_id" to "defghijklmnop", "commit_message" to "A Commit message", "commit_author" to "Amandeep"))

        Assert.assertEquals("Objects are not equal", apkCache, secondCache)
    }

    @Test
    fun equalsMethodFailureTest() {
        val apkCache = ApkCache(mapOf("APP_URL" to "https://abc.com/a/b", "PROGUARD" to "true", "BRANCH" to "development",
            "commit_id" to "abcdefghijklmnop", "commit_message" to "A Commit message", "commit_author" to "Amandeep"),
            "/Users/app/abc/application.apk")
        val secondCache = ApkCache(mapOf("APP_URL" to "https://abc.com/a/b", "PROGUARD" to "true", "BRANCH" to "master",
            "commit_id" to "abcdefghijklmnop", "commit_message" to "A Commit message", "commit_author" to "Amandeep"),
            "/Users/app/abc/application.apk")

        Assert.assertNotEquals("Objects are equal", apkCache, secondCache)
    }

    @Test
    fun findElementTest() {
        val apkCache1 = ApkCache(mapOf("APP_URL" to "https://abc.com/a/b", "PROGUARD" to "true", "BRANCH" to "master",
            "commit_id" to "abcdefghijklmnop", "commit_message" to "A Commit message", "commit_author" to "Amandeep"),
            "/Users/app/abc/application1.apk")
        val apkCache2 = ApkCache(mapOf("APP_URL" to "https://abc.com/a/b", "PROGUARD" to "true", "BRANCH" to "development",
            "commit_id" to "abcdefghijklmnop", "commit_message" to "A Commit message", "commit_author" to "Amandeep"),
            "/Users/app/abc/application2.apk")
        val apkCache3 = ApkCache(mapOf("APP_URL" to "https://abc.com/a/b1", "PROGUARD" to "true", "BRANCH" to "development",
            "commit_id" to "abcdefghijklmnop", "commit_message" to "A Commit message", "commit_author" to "Amandeep"),
            "/Users/app/abc/application3.apk")
        val apkCache4 = ApkCache(mapOf("APP_URL" to "https://abc.com/a/b2", "PROGUARD" to "true", "BRANCH" to "development",
            "commit_id" to "abcdefghijklmnop", "commit_message" to "A Commit message", "commit_author" to "Amandeep"),
            "/Users/app/abc/application4.apk")
        val apkCache5 = ApkCache(mapOf("BRANCH" to "development", "APP_URL" to "https://abc.com/a/b",
            "commit_id" to "abcdefghijklmnop", "commit_message" to "A Commit message", "commit_author" to "Amandeep"),
            "/Users/app/abc/application5.apk")

        val list = listOf(apkCache1, apkCache2, apkCache3, apkCache4, apkCache5)
        val toTest = ApkCache(mapOf("APP_URL" to "https://abc.com/a/b", "BRANCH" to "development"))
        assertEquals("/Users/app/abc/application5.apk", list.firstOrNull {
            it == toTest
        }?.pathOnDisk, "Not equal")
    }
}