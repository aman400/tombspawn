package com.tombspawn.di

import com.github.dockerjava.api.DockerClient
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.di.qualifiers.DockerHttpClient
import dagger.Module
import dagger.Provides
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import org.mockito.Mockito.mock

@Module
class FakeDockerModule {

    @AppScope
    @Provides
    fun provideFakeDockerClient(): DockerClient {
        return mock(DockerClient::class.java)
    }

    @AppScope
    @DockerHttpClient
    @Provides
    fun provideDockerClient(): MutableMap<String, HttpClient> {
        return mutableMapOf("a" to HttpClient(MockEngine) {
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
        })
    }
}