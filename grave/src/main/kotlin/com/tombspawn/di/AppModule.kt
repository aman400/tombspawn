package com.tombspawn.di

import com.google.common.base.Optional
import com.google.gson.reflect.TypeToken
import com.tombspawn.base.config.JsonApplicationConfig
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.base.network.Common.createHttpClient
import com.tombspawn.data.DatabaseService
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
import java.io.File

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
    @UploadDir
    fun provideTempUploadDir(): File {
        return File(System.getProperty("user.dir"), "temp")
    }

    @Provides
    @AppScope
    @WaitingMessages
    fun provideWaitingMessages(config: JsonApplicationConfig): Optional<List<String>> {
        val messages: List<String>? = config.propertyOrNull("waiting_messages")
            ?.getAs<List<String>>(object : TypeToken<List<String>>() {}.type).takeIf {
                !it.isNullOrEmpty()
            }
        return Optional.fromNullable(messages)

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
}