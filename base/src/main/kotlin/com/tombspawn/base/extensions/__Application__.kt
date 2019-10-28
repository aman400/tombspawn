package com.tombspawn.base.extensions

import io.ktor.application.Application

val Application.envKind get() = environment.config.property("ktor.deployment.environment").getString()
val Application.isDebug get() = envKind == "debug"
val Application.port get() = environment.config.propertyOrNull("ktor.deployment.port")?.getString()?.toIntOrNull()