package com.tombspawn.di

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.netty.NettyDockerCmdExecFactory
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.base.network.Common
import com.tombspawn.di.qualifiers.Debuggable
import com.tombspawn.di.qualifiers.DockerHttpClient
import com.tombspawn.models.config.App
import com.tombspawn.utils.Constants
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
        ).build()
    }

    @Provides
    @AppScope
    @DockerHttpClient
    fun provideDockerHttpClients(gsonSerializer: GsonSerializer, apps: List<App>, @Debuggable isDebug: Boolean): MutableMap<String, HttpClient> {
        val dockerHttpClients = mutableMapOf<String, HttpClient>()
        apps.forEach { app ->
            dockerHttpClients[app.id] = Common.createHttpClient(
                gsonSerializer, app.containerUri ?: let {
                    "${app.id}:${Constants.Common.DEFAULT_PORT}"
                }, URLProtocol.HTTP, null,
                120000, 120000, 120000, isDebug
            )
        }
        return dockerHttpClients
    }
}