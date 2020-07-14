package com.tombspawn.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.data.cache.models.ApkCache
import com.tombspawn.di.qualifiers.ApkCacheMap
import com.tombspawn.di.qualifiers.AppCacheMap
import com.tombspawn.models.Reference
import org.slf4j.LoggerFactory
import java.util.regex.Pattern
import javax.inject.Inject

@AppScope
class CachingService @Inject constructor(
    @AppCacheMap private val cacheMap: StringMap,
    @ApkCacheMap private val apkCacheMap: StringMap,
    private val gson: Gson
) {
    private val LOGGER = LoggerFactory.getLogger("com.tombspawn.data.CachingService")

    fun cacheAppReferences(appId: String, refs: List<Reference>) {
        try {
            cacheMap.setData(
                getReferencesCacheKey(appId),
                gson.toJson(refs, object : TypeToken<List<Reference>>() {}.type)
            )
        } catch (exception: Exception) {
            LOGGER.error("Unable to cache App References", exception)
        }
    }

    fun getCachedReferences(appId: String): List<Reference>? {
        return try {
            cacheMap.getData(getReferencesCacheKey(appId)).takeIf {
                !it.isNullOrEmpty()
            }?.let {
                LOGGER.debug("References: Cache hit")
                gson.fromJson<List<Reference>>(it, object : TypeToken<List<Reference>>() {}.type)
            }
        } catch (exception: Exception) {
            LOGGER.error("Unable to get cached References", exception)
            null
        }
    }

    fun cacheApk(appId: String, branch: String, apkCache: ApkCache) {
        try {
            val list = getApkCache(appId, branch)
            list.add(apkCache)
            apkCacheMap.setData(
                getAppCacheMapKey(appId, branch),
                gson.toJson(list, object : TypeToken<List<ApkCache>>() {}.type)
            )
        } catch (exception: Exception) {
            LOGGER.error("Unable to cache apk", exception)
        }
    }

    fun getApkCache(appId: String, branch: String): MutableList<ApkCache> {
        return try {
            apkCacheMap.getData(getAppCacheMapKey(appId, branch))?.let {
                gson.fromJson<MutableList<ApkCache>>(it, object : TypeToken<MutableList<ApkCache>>() {}.type)
            } ?: mutableListOf()
        } catch (exception: Exception) {
            LOGGER.error("Unable to fetch App Cache list", exception)
            mutableListOf()
        }
    }

    fun deleteApkCache(appId: String, branch: String, apkCache: ApkCache? = null) {
        try {
            if (apkCache != null) {
                val list = getApkCache(appId, branch)
                list.remove(apkCache)
                apkCacheMap.setData(
                    getAppCacheMapKey(appId, branch),
                    gson.toJson(list, object : TypeToken<List<ApkCache>>() {}.type)
                )
            } else {
                apkCacheMap.deleteKey(getAppCacheMapKey(appId, branch))
            }
        } catch (exception: Exception) {
            LOGGER.error("Unable to delete App cache", exception)
        }
    }

    fun close() {
        try {
            cacheMap.close()
        } catch (exception: Exception) {
            LOGGER.error("Unable to close cache map", exception)
        }
    }

    companion object {

        fun getAppCacheMapKey(appId: String, branch: String): String {
            return "${appId}__${branch}"
        }

        fun getReferencesCacheKey(appId: String): String {
            return "${appId}_references"
        }

        fun getFlavoursCacheKey(appId: String): String {
            return "${appId}_flavours"
        }

        fun getBuildVariantCacheKey(appId: String): String {
            return "${appId}_build_variants"
        }
    }
}