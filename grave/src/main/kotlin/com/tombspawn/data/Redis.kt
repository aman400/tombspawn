package com.tombspawn.data

import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

abstract class Redis<T> constructor(private val redisClient: RedissonClient) {
    private val LOGGER = LoggerFactory.getLogger("com.tombspawn.data.Redis")

    abstract fun getData(key: String): T?

    abstract fun setData(key: String, value: T, ttl: Long? = null, timeUnit: TimeUnit? = null)

    abstract fun deleteKey(key: String)

    fun close() {
        try {
            redisClient.shutdown()
        } catch (exception: Exception) {
            LOGGER.error("Unable to close reddison client", exception)
        }
    }
}