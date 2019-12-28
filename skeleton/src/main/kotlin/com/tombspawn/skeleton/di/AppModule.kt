package com.tombspawn.skeleton.di

import com.tombspawn.base.config.JsonApplicationConfig
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.base.network.Common
import com.tombspawn.skeleton.di.qualifiers.InitCallbackUri
import com.tombspawn.skeleton.di.qualifiers.FileUploadDir
import com.tombspawn.skeleton.di.qualifiers.UploadAppClient
import com.tombspawn.skeleton.git.CredentialProvider
import com.tombspawn.skeleton.models.App
import com.tombspawn.skeleton.models.config.CommonConfig
import dagger.Module
import dagger.Provides
import io.ktor.application.Application
import io.ktor.client.HttpClient
import io.ktor.client.features.json.GsonSerializer

@Module
object AppModule {

    @Provides
    @AppScope
    fun provideEnvironment(application: Application): JsonApplicationConfig {
        return application.environment.config as JsonApplicationConfig
    }

    @Provides
    @InitCallbackUri
    @AppScope
    fun provideInitCallbackUri(config: JsonApplicationConfig): String {
        return config.property("init_callback_uri").getString()
    }

    @Provides
    @AppScope
    fun provideSlack(config: JsonApplicationConfig): CommonConfig {
        return config.property("common")
            .getAs(CommonConfig::class.java)
    }

    @Provides
    @AppScope
    fun provideCredentialProvider(config: JsonApplicationConfig): CredentialProvider {
        return config.property("git")
            .getAs(CredentialProvider::class.java)
    }

    @Provides
    @AppScope
    fun provideApp(config: JsonApplicationConfig): App {
        return config.property("app").getAs(App::class.java)
    }

    @Provides
    @AppScope
    @UploadAppClient
    fun provideAppUploadClient(gsonSerializer: GsonSerializer): HttpClient {
        return Common.createHttpClient(gsonSerializer, null, null, null)
    }

    @Provides
    @AppScope
    @FileUploadDir
    fun provideUploadDirPath(): String {
        return "${System.getProperty("user.dir")}/temp"
    }
}