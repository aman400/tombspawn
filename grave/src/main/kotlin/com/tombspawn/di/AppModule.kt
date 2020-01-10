package com.tombspawn.di

import com.google.common.base.Optional
import com.tombspawn.base.config.JsonApplicationConfig
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.base.network.Common.createHttpClient
import com.tombspawn.data.DatabaseService
import com.tombspawn.data.StringMap
import com.tombspawn.di.qualifiers.*
import com.tombspawn.git.CredentialProvider
import com.tombspawn.models.config.*
import com.tombspawn.utils.Constants
import dagger.Module
import dagger.Provides
import io.ktor.application.Application
import io.ktor.client.HttpClient
import io.ktor.client.features.json.GsonSerializer
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config


@Module
class AppModule {

    @Provides
    @AppScope
    fun provideEnvironment(application: Application): JsonApplicationConfig {
        return application.environment.config as JsonApplicationConfig
    }

    @Provides
    @AppScope
    fun provideSlack(config: JsonApplicationConfig): Slack {
        return config.property("slack").getAs(Slack::class.java)
    }

    @Provides
    @AppScope
    fun provideDb(config: JsonApplicationConfig): Db {
        return config.property("db").getAs(Db::class.java)
    }

    @Provides
    @AppScope
    fun provideDatabase(db: Db, @Debuggable isDebug: Boolean): DatabaseService {
        return DatabaseService(db.url, db.username, db.password, isDebug)
    }

    @Provides
    @AppScope
    fun provideCommon(config: JsonApplicationConfig): Common {
        return config.property("common").getAs(Common::class.java)
    }

    @Provides
    @AppScope
    fun provideRedisConfig(config: JsonApplicationConfig): Redis {
        return config.property("redis").getAs(Redis::class.java)
    }

    @Provides
    @AppScope
    fun provideCredentialProvider(config: JsonApplicationConfig): CredentialProvider {
        return config.property("git").getAs(CredentialProvider::class.java)
    }

    @Provides
    @AppScope
    @SlackHttpClient
    fun provideSlackHttpClient(gsonSerializer: GsonSerializer, @Debuggable isDebug: Boolean): HttpClient {
        return createHttpClient(
            gsonSerializer, "slack.com", URLProtocol.HTTPS, null, enableLogger = isDebug
        )
    }

    @Provides
    @AppScope
    @UploadDirPath
    fun provideUploadDirPath(): String {
        return "${System.getProperty("user.dir")}/temp"
    }

    @Provides
    @AppScope
    @Debuggable
    fun provideIsDebug(serverConf: Optional<ServerConf>): Boolean {
        return serverConf.get()?.debug ?: false
    }

    @Provides
    @ApplicationBaseUri
    fun provideApplicationBaseUri(@Debuggable debug: Boolean, serverConf: Optional<ServerConf>): URLBuilder {
        return URLBuilder().apply {
            if (debug) {
                this.protocol = serverConf.get()?.scheme?.let {
                    URLProtocol.createOrDefault(it)
                } ?: URLProtocol.HTTP
                this.host = serverConf.get().hostName ?: Constants.Common.DEFAULT_HOST
                this.port = serverConf.get().port ?: Constants.Common.DEFAULT_PORT
            } else {
                this.protocol = URLProtocol.HTTP
                this.host = serverConf.get().hostName ?: "application"
                this.port = serverConf.get()?.port ?: Constants.Common.DEFAULT_PORT
            }
        }
    }

    @Provides
    @AppScope
    fun provideAppList(config: JsonApplicationConfig): List<App> {
        return config.configList("apps").map {

            it.getAs(App::class.java)
        }
    }

    @Provides
    @AppScope
    fun provideServerConf(config: JsonApplicationConfig): Optional<ServerConf> {
        return Optional.fromNullable(config.propertyOrNull("server")?.getAs(ServerConf::class.java))
    }

    @Provides
    @AppScope
    fun provideRedisClient(redis: Redis): RedissonClient {
        val config = Config()
        config.useSingleServer().apply {
            timeout = 1000000
            address = "${redis.host ?: Constants.Common.DEFAULT_REDIS_HOST}:${redis.port?: Constants.Common.DEFAULT_REDIS_PORT}"
        }
        return Redisson.create(config)
    }

    @Provides
    @AppScope
    @AppCacheMap
    fun provideRedisAppCacheMap(redissonClient: RedissonClient): StringMap {
        return StringMap("AppCache", redissonClient)
    }

    @Provides
    @AppScope
    @ApkCacheMap
    fun provideRedisApkCacheMap(redissonClient: RedissonClient): StringMap {
        return StringMap("ApkCache", redissonClient)
    }
}