package com.ramukaka.data

import org.redisson.api.RMapCache
import org.redisson.api.RedissonClient
import org.redisson.api.map.event.EntryExpiredListener
import java.util.concurrent.TimeUnit

class Redis(private val redissonClient: RedissonClient, private val keyExpiryListener: EntryExpiredListener<String, Any>) {
    companion object {
        private const val ARG_STRING_MAP = "string_map"
    }

    fun getStringData(key: String): String? {
        val map: RMapCache<String, String> = redissonClient.getMapCache(ARG_STRING_MAP)
        val data = map[key]
        map.destroy()
        return data
    }

    fun setStringData(key: String, value: String, ttl: Long? = null, timeUnit: TimeUnit? = null) {
        val map: RMapCache<String, String> = redissonClient.getMapCache(ARG_STRING_MAP)
        if(ttl != null && timeUnit != null) {
            map.put(key, value, ttl, timeUnit)
        } else {
            map[key] = value
        }
        map.addListener(keyExpiryListener)
        map.destroy()
    }
}