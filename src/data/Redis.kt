package com.ramukaka.data

import org.redisson.api.RedissonClient
import org.redisson.api.map.event.EntryExpiredListener
import java.util.concurrent.TimeUnit

abstract class Redis<T>(val redissonClient: RedissonClient, val map: String) {
    companion object {
        const val AUTH_MAP = "auth_map"
        const val SESSION_MAP = "session_map"
    }

    abstract fun getData(key: String): T?

    abstract fun setData(key: String, value: T, ttl: Long? = null, timeUnit: TimeUnit? = null,
                      keyExpiryListener: EntryExpiredListener<String, Any>? = null)
}