package com.tombspawn.di

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.netty.NettyDockerCmdExecFactory
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.base.di.scopes.CoroutineScope
import com.tombspawn.base.network.Common
import com.tombspawn.di.qualifiers.DockerHttpClient
import com.tombspawn.models.config.App
import com.tombspawn.network.docker.DockerApiClient
import dagger.Module
import dagger.Provides
import io.ktor.client.HttpClient
import io.ktor.client.features.json.GsonSerializer
import io.ktor.http.URLProtocol

@Module
class DockerModule {

    @Provides
    @AppScope
    fun provideDockerClient(): DockerClient {
        val config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .build()
        return DockerClientBuilder.getInstance(config).withDockerCmdExecFactory(
            NettyDockerCmdExecFactory()
                .withConnectTimeout(60000)
                .withReadTimeout(60000)
        ).build()
    }

    @Provides
    @AppScope
    @DockerHttpClient
    fun provideDockerHttpClients(gsonSerializer: GsonSerializer, apps: List<App>): MutableMap<String, HttpClient> {
        val dockerHttpClients = mutableMapOf<String, HttpClient>()
        apps.forEach {
            dockerHttpClients[it.id] = Common.createHttpClient(
                gsonSerializer, it.appUrl, URLProtocol.HTTP, null,
                240_000, 240_000, 120_000
            )
        }
        return dockerHttpClients
    }
}