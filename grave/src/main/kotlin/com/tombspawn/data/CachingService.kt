package com.tombspawn.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.di.qualifiers.AppCacheMap
import com.tombspawn.models.Reference
import com.tombspawn.models.redis.ApkCallbackCache
import org.slf4j.LoggerFactory
import javax.inject.Inject

@AppScope
class CachingService @Inject constructor(@AppCacheMap val cacheMap: StringMap,
                                         val gson: Gson) {
    private val LOGGER = LoggerFactory.getLogger("com.tombspawn.data.CachingService")

    fun cacheAppReferences(appId: String, refs: List<Reference>) {
        cacheMap.setData(StringMap.getReferencesCacheKey(appId),
            gson.toJson(refs, object: TypeToken<List<Reference>>() {}.type))
    }

    fun getCachedReferences(appId: String): List<Reference>? {
        return cacheMap.getData(StringMap.getReferencesCacheKey(appId)).takeIf {
            !it.isNullOrEmpty()
        }?.let {
            LOGGER.debug("References: Cache hit")
            gson.fromJson<List<Reference>>(it, object: TypeToken<List<Reference>>() {}.type)
        }
    }

    fun getCachedFlavours(appId: String): List<String>? {
        return cacheMap.getData(StringMap.getFlavoursCacheKey(appId)).takeIf {
            !it.isNullOrEmpty()
        }?.let {
            LOGGER.debug("Flavours: Cache hit")
            gson.fromJson<List<String>>(it, object: TypeToken<List<String>>() {}.type)
        }
    }

    fun getBuildVariants(appId: String): List<String>? {
        return cacheMap.getData(StringMap.getBuildVariantCacheKey(appId)).takeIf {
            !it.isNullOrEmpty()
        }?.let {
            LOGGER.debug("Build Variants: Cache hit")
            gson.fromJson<List<String>>(it, object: TypeToken<List<String>>() {}.type)
        }
    }

    fun cacheBuildVariants(appId: String, buildVariants: List<String>) {
        cacheMap.setData(StringMap.getBuildVariantCacheKey(appId),
            gson.toJson(buildVariants, object: TypeToken<List<String>>() {}.type))
    }

    fun cacheAppFlavours(appId: String, flavours: List<String>) {
        cacheMap.setData(StringMap.getFlavoursCacheKey(appId),
            gson.toJson(flavours, object: TypeToken<List<String>>() {}.type))
    }

    fun saveAppCallbackCache(callbackId: String, responseUrl: String, channelId: String) {
        cacheMap.setData(
            callbackId, gson.toJson(
                ApkCallbackCache(callbackId, responseUrl, channelId),
                ApkCallbackCache::class.java
            ).toString()
        )
    }

    fun getAppCallbackCache(callbackId: String): ApkCallbackCache? {
        return cacheMap.getData(callbackId)?.let {
            try {
                gson.fromJson<ApkCallbackCache>(it, ApkCallbackCache::class.java)
            } catch (exception: Exception) {
                LOGGER.error("Callback cache missing", exception)
                null
            }
        }
    }

    fun clearAppCallback(callbackId: String) {
        cacheMap.delete(callbackId)
    }

    fun close() {
        cacheMap.close()
    }
}