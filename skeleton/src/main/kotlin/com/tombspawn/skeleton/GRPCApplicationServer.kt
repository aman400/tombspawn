package com.tombspawn.skeleton

import io.grpc.Server
import io.grpc.ServerBuilder
import io.ktor.application.ApplicationStopPreparing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.ApplicationEngineEnvironment
import io.ktor.server.engine.ApplicationEngineFactory
import io.ktor.server.engine.BaseApplicationEngine
import java.util.concurrent.TimeUnit

object GRPC : ApplicationEngineFactory<GRPCApplicationEngine, GRPCApplicationEngine.Configuration> {
    override fun create(
        environment: ApplicationEngineEnvironment,
        configure: GRPCApplicationEngine.Configuration.() -> Unit
    ): GRPCApplicationEngine {
        return GRPCApplicationEngine(environment, configure)
    }
}

class GRPCApplicationEngine constructor(
    environment: ApplicationEngineEnvironment,
    configure: Configuration.() -> Unit = {}
) : BaseApplicationEngine(environment) {

    class Configuration : BaseApplicationEngine.Configuration() {
        var configuration: ServerBuilder<*>.() -> Unit = {}
    }

    private val configuration = Configuration().apply {
        configure.invoke(this)
    }
    private var server: Server? = null

    override fun start(wait: Boolean): ApplicationEngine {
        environment.start()
        server = ServerBuilder
            .forPort(environment.connectors.first().port)
            .apply {
                configuration.configuration.invoke(this)
            }
            .build()

        server!!.start()

        if (wait) {
            server!!.awaitTermination()
        }

        return this
    }

    override fun stop(gracePeriodMillis: Long, timeoutMillis: Long) {
        environment.monitor.raise(ApplicationStopPreparing, environment)

        server?.also {
            it.awaitTermination(gracePeriodMillis, TimeUnit.MICROSECONDS)

            it.shutdownNow()
        }
        environment.stop()
    }
}