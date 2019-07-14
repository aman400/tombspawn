package com.ramukaka.extensions

import io.ktor.application.Application

val Application.envKind get() = environment.config.property("ktor.deployment.environment").getString()
val Application.isDebug get() = envKind == "debug"