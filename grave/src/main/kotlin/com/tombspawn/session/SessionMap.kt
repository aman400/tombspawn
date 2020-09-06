package com.tombspawn.session

import com.tombspawn.data.Redis
import org.redisson.api.LocalCachedMapOptions
import org.redisson.api.RLocalCachedMap
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class SessionMap constructor(val key: String, redisClient: RedissonClient): Redis<ByteArray>(redisClient) {
    private val LOGGER = LoggerFactory.getLogger("com.tombspawn.session.SessionMap")

    private val sessionMap: RLocalCachedMap<String, ByteArray>

    init {
        val cachingOptions = LocalCachedMapOptions.defaults<String, ByteArray>()
        cachingOptions.evictionPolicy(LocalCachedMapOptions.EvictionPolicy.NONE)
        cachingOptions.reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.NONE)
        cachingOptions.syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE)
        sessionMap = redisClient.getLocalCachedMap(key, cachingOptions)
    }

    override fun setData(key: String, value: ByteArray, ttl: Long?, timeUnit: TimeUnit?) {
        sessionMap[key] = value
    }

    override fun getData(key: String): ByteArray? {
        return sessionMap[key]
    }

    override fun deleteKey(key: String) {
        sessionMap.remove(key)
    }
}