package com.tombspawn.auth

import com.tombspawn.data.StringMap
import java.util.concurrent.TimeUnit

class RedisSessionStorage(val redis: StringMap, val prefix: String = "session_",
                          val ttlSeconds: Long = 3600) : SimplifiedSessionStorage() {
    private fun buildKey(id: String) = "$prefix$id"

    override suspend fun read(id: String): ByteArray? {
        val key = buildKey(id)
        return redis.getData(key)?.toByteArray(Charsets.UTF_8)
    }

    override suspend fun write(id: String, data: ByteArray?) {
        val key = buildKey(id)
        if (data == null) {
            redis.getData(key)
        } else {
            redis.setData(key, data.toString(Charsets.UTF_8), ttlSeconds, TimeUnit.SECONDS)
        }
    }
}