package com.tombspawn.data

import redis.clients.jedis.Jedis
import java.util.concurrent.TimeUnit

class StringMap constructor(jedis: Jedis): Redis<String>(jedis) {

    override fun getData(key: String): String? {
        return jedis.get(key)
    }

    override fun setData(key: String, value: String, ttl: Long?, timeUnit: TimeUnit?) {
        if (ttl != null && timeUnit != null) {
            val seconds = TimeUnit.SECONDS.convert(ttl, timeUnit)
            jedis.setex(key, seconds.toInt(), value)
        } else {
            jedis.set(key, value)
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