package com.tombspawn.data

import org.redisson.api.RMap
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class StringMap constructor(val key: String, redisClient: RedissonClient): Redis<String>(redisClient) {
    private val LOGGER = LoggerFactory.getLogger("com.tombspawn.data.StringMap")

    private val stringMap: RMap<String, String> = redisClient.getMap<String, String>(key)

    override fun getData(key: String): String? {
        return stringMap[key]
    }

    override fun deleteKey(key: String) {
        stringMap.remove(key)
    }

    override fun setData(key: String, value: String, ttl: Long?, timeUnit: TimeUnit?) {
        stringMap[key] = value
    }

    companion object {
        
        fun getAppCacheMapKey(appId: String, branch: String): String {
            return "${appId}_refs_${branch}"
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