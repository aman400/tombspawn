package com.tombspawn.data

import redis.clients.jedis.Jedis
import java.util.concurrent.TimeUnit

abstract class Redis<T>(val jedis: Jedis) {
    abstract fun getData(key: String): T?

    abstract fun setData(key: String, value: T, ttl: Long? = null, timeUnit: TimeUnit? = null)

    fun close() {
        jedis.close()
    }

    open fun delete(key: String) {
        jedis.del(key)
    }
}