package com.ramukaka.data

import org.redisson.api.RMapCache
import org.redisson.api.RedissonClient
import org.redisson.api.map.event.EntryExpiredListener
import java.util.concurrent.TimeUnit

class StringMap(redissonClient: RedissonClient, map: String): Redis<String>(redissonClient, map) {

    override fun getData(key: String): String? {
        val map: RMapCache<String, String> = redissonClient.getMapCache(map)
        val data = map[key]
        map.destroy()
        return data
    }

    override fun setData(key: String, value: String, ttl: Long?, timeUnit: TimeUnit?,
                keyExpiryListener: EntryExpiredListener<String, Any>?) {
        val map: RMapCache<String, String> = redissonClient.getMapCache(map)
        if(ttl != null && timeUnit != null) {
            map.fastPut(key, value, ttl, timeUnit)
        } else {
            map.fastPut(key, value)
        }
        keyExpiryListener?.let {
            map.addListener(it)
        }
        map.destroy()
    }
}