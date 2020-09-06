package com.tombspawn.data

import org.redisson.api.LocalCachedMapOptions
import org.redisson.api.RLocalCachedMap
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class StringMap constructor(val key: String, redisClient: RedissonClient): Redis<String>(redisClient) {
    private val LOGGER = LoggerFactory.getLogger("com.tombspawn.data.StringMap")

    private val stringMap: RLocalCachedMap<String, String>

    init {
        val cachingOptions = LocalCachedMapOptions.defaults<String, String>()
        cachingOptions.evictionPolicy(LocalCachedMapOptions.EvictionPolicy.NONE)
        cachingOptions.reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.NONE)
        cachingOptions.syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)
        stringMap = redisClient.getLocalCachedMap(key, cachingOptions)
    }

    override fun getData(key: String): String? {
        return stringMap[key]
    }

    override fun deleteKey(key: String) {
        stringMap.remove(key)
    }

    override fun setData(key: String, value: String, ttl: Long?, timeUnit: TimeUnit?) {
        stringMap[key] = value
    }
}