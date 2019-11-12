package com.tombspawn.di

import com.tombspawn.base.config.JsonApplicationConfig
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.base.network.Common.createHttpClient
import com.tombspawn.data.DatabaseService
import com.tombspawn.di.qualifiers.Debuggable
import com.tombspawn.di.qualifiers.SlackHttpClient
import com.tombspawn.di.qualifiers.UploadDirPath
import com.tombspawn.git.CredentialProvider
import com.tombspawn.models.config.*
import dagger.Module
import dagger.Provides
import io.ktor.application.Application
import io.ktor.client.HttpClient
import io.ktor.client.features.json.GsonSerializer
import io.ktor.http.URLProtocol

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
    fun provideSlackHttpClient(gsonSerializer: GsonSerializer): HttpClient {
        return createHttpClient(gsonSerializer, "slack.com",
            URLProtocol.HTTPS, null)
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
    fun provideIsDebug(config: JsonApplicationConfig): Boolean {
        return config.propertyOrNull("server")?.getAs(ServerConf::class.java)?.let {
            it.debug == true
        } ?: false
    }

    @Provides
    @AppScope
    fun provideAppList(config: JsonApplicationConfig): List<App> {
        return config.configList("apps").map {
            it.getAs(App::class.java)
        }
    }
}