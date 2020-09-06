package com.tombspawn.di

import com.tombspawn.data.StringMap
import com.tombspawn.di.qualifiers.ApkCacheMap
import com.tombspawn.di.qualifiers.AppCacheMap
import com.tombspawn.di.qualifiers.SessionMapKey
import com.tombspawn.models.config.Redis
import com.tombspawn.session.RedisSessionStorage
import com.tombspawn.session.SessionMap
import com.tombspawn.utils.Constants
import dagger.Module
import dagger.Provides
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config

@Module
class CachingModule {

    @Provides
    fun provideRedisClient(redis: Redis): RedissonClient {
        val config = Config()
        config.useSingleServer().apply {
            timeout = 10000
            address = "${redis.host ?: Constants.Common.DEFAULT_REDIS_HOST}:${redis.port?: Constants.Common.DEFAULT_REDIS_PORT}"
        }
        return Redisson.create(config)
    }

    @Provides
    @AppCacheMap
    fun provideRedisAppCacheMap(redissonClient: RedissonClient): StringMap {
        return StringMap("AppCache", redissonClient)
    }

    @Provides
    @ApkCacheMap
    fun provideRedisApkCacheMap(redissonClient: RedissonClient): StringMap {
        return StringMap("ApkCache", redissonClient)
    }

    @Provides
    @SessionMapKey
    fun providesRedisSession(redissonClient: RedissonClient): SessionMap {
        return SessionMap("SessionStorage", redissonClient)
    }

    @Provides
    fun provideRedisSessionStorage(@SessionMapKey sessionMap: SessionMap): RedisSessionStorage {
        return RedisSessionStorage(sessionMap)
    }
}