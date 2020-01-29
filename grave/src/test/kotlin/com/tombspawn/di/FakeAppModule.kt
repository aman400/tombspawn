package com.tombspawn.di

import com.google.common.base.Optional
import com.google.gson.Gson
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.di.qualifiers.ApplicationBaseUri
import com.tombspawn.di.qualifiers.Debuggable
import com.tombspawn.di.qualifiers.SlackHttpClient
import com.tombspawn.di.qualifiers.UploadDir
import com.tombspawn.git.CredentialProvider
import com.tombspawn.models.config.App
import com.tombspawn.models.config.Common
import com.tombspawn.models.config.ServerConf
import com.tombspawn.models.config.Slack
import dagger.Module
import dagger.Provides
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.mock
import java.io.File

@Module
class FakeAppModule {

    @Rule val tempFolder = TemporaryFolder()

    @AppScope
    @Provides
    fun provideGson(): Gson {
        return Gson()
    }

    @AppScope
    @Provides
    fun provideSlack(): Slack {
        return mock(Slack::class.java)
    }

    @AppScope
    @Provides
    fun provideCommonConfig(): Common {
        return mock(Common::class.java)
    }

    @AppScope
    @Provides
    fun provideFakeAppConfig(): List<App> {
        return listOf(App("test", "test", "1"))
    }

    @AppScope
    @UploadDir
    @Provides
    fun provideFakeUploadDir(): File {
        return tempFolder.newFolder("temp")
    }

    @Provides
    @Debuggable
    fun provideIsDebuggable(): Boolean {
        return true
    }

    @Provides
    fun provideFakeServerConf(): Optional<ServerConf> {
        return Optional.of(mock(ServerConf::class.java))
    }

    @Provides
    @ApplicationBaseUri
    fun provideFakeApplicationBaseUri(): URLBuilder {
        return mock(URLBuilder::class.java)
    }

    @Provides
    fun provideFakeCredentialProvider(): CredentialProvider {
        return mock(CredentialProvider::class.java)
    }

    @Provides
    @SlackHttpClient
    fun provideFakeSlackHttpClient(): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    when (request.url.fullPath) {
                        "https://example.org/" -> {
                            val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Text.Plain.toString()))
                            respond("Hello, world", headers = responseHeaders)
                        }
                        else -> error("Unhandled ${request.url.fullPath}")
                    }
                }
            }
        }
    }
}