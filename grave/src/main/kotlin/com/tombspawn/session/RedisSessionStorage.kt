package com.tombspawn.session

class RedisSessionStorage(
    val redis: SessionMap,
    val prefix: String = "session_"
) : SimplifiedSessionStorage() {
    private fun buildKey(id: String) = "$prefix$id"

    override suspend fun read(id: String): ByteArray? {
        val key = buildKey(id)
        return redis.getData(key)
    }

    override suspend fun write(id: String, data: ByteArray?) {
        val key = buildKey(id)
        if (data == null) {
            redis.deleteKey(buildKey(id))
        } else {
            redis.setData(key, data)
        }
    }
}
