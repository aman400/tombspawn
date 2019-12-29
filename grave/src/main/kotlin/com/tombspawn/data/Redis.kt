package com.tombspawn.data

import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import java.util.concurrent.TimeUnit
import javax.inject.Provider

abstract class Redis<T> constructor(private val jedisPool: Provider<JedisPool>) {
    private val LOGGER = LoggerFactory.getLogger("com.tombspawn.data.Redis")

    var jedis: Jedis = jedisPool.get().resource
    abstract fun getData(key: String): T?

    abstract fun setData(key: String, value: T, ttl: Long? = null, timeUnit: TimeUnit? = null)

    fun close() {
        try {
            jedis.close()
        } catch (exception: Exception) {
            LOGGER.error("Unable to close jedis", exception)
        }
    }

    fun recreateInstance() {
        LOGGER.info("Recreating new instance")
        jedis = jedisPool.get().resource
    }

    open fun delete(key: String) {
        jedis.del(key)
    }
}