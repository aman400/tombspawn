package com.tombspawn.models.config

import org.junit.Assert
import org.junit.Test

class TestApp {
    @Test
    fun checkDockerEnvVariables() {
        var app = App("1", "test", "w122", env = listOf("A=B", "C=D", "E=F"))
        Assert.assertEquals("Variables are not equal", """
            |ENV A B
            |ENV C D
            |ENV E F
        """.trimMargin(), app.dockerEnvVariables())

        app = App("1", "test", "w122", env = listOf("SENTRY_AUTH_TOKEN=test",
            "SENTRY_ORG=v-9u",
            "SENTRY_PROJECT=consumer-android",
            "SENTRY_LOG_LEVEL=debug",
            "PATH=/app/git/consumer:${'$'}PATH"))
        Assert.assertEquals("Variables are not equal", """
            |ENV SENTRY_AUTH_TOKEN test
            |ENV SENTRY_ORG v-9u
            |ENV SENTRY_PROJECT consumer-android
            |ENV SENTRY_LOG_LEVEL debug
            |ENV PATH /app/git/consumer:${'$'}PATH
        """.trimMargin(), app.dockerEnvVariables())

        app = App("1", "test", "w122")
        Assert.assertNotNull(app.dockerEnvVariables())

        app = App("1", "test", "w122", env = listOf("SENTRY_AUTH_TOKEN"))
        Assert.assertEquals("", app.dockerEnvVariables())
    }
}