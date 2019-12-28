package com.tombspawn.data

import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import java.util.concurrent.TimeUnit

class StringMap constructor(jedis: Jedis): Redis<String>(jedis) {
    private val LOGGER = LoggerFactory.getLogger("com.tombspawn.data.StringMap")
    override fun getData(key: String): String? {
        return try {
            jedis.get(key)
        } catch (exception: Exception) {
            LOGGER.error("Jedis not connecting", exception)
            null
        }
    }

    override fun setData(key: String, value: String, ttl: Long?, timeUnit: TimeUnit?) {
        try {
            if (ttl != null && timeUnit != null) {
                val seconds = TimeUnit.SECONDS.convert(ttl, timeUnit)
                jedis.setex(key, seconds.toInt(), value)
            } else {
                jedis.set(key, value)
            }
        } catch (exception: Exception) {
            LOGGER.error("Unable to set key: $key \nvalue: $value", exception)
        }
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