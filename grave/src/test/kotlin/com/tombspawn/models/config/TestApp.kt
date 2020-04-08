package com.tombspawn.models.config

import org.junit.Assert
import org.junit.Test

class TestApp {
    @Test
    fun checkDockerEnvVariables() {
        var app = App("1", "test", "w122", env = mapOf("A" to "B", "C" to "D", "E" to "F"))
        Assert.assertEquals("Variables are not equal", """
            |ENV A B
            |ENV C D
            |ENV E F
        """.trimMargin(), app.dockerEnvVariables())

        app = App("1", "test", "w122", env = mapOf("SENTRY_AUTH_TOKEN" to "test",
            "SENTRY_ORG" to "v-9u",
            "SENTRY_PROJECT" to "consumer-android",
            "SENTRY_LOG_LEVEL" to "debug",
            "PATH" to "/app/git/consumer:${'$'}PATH")
        )
        Assert.assertEquals("Variables are not equal", """
            |ENV SENTRY_AUTH_TOKEN test
            |ENV SENTRY_ORG v-9u
            |ENV SENTRY_PROJECT consumer-android
            |ENV SENTRY_LOG_LEVEL debug
            |ENV PATH /app/git/consumer:${'$'}PATH
        """.trimMargin(), app.dockerEnvVariables())

        app = App("1", "test", "w122")
        Assert.assertNotNull(app.dockerEnvVariables())

        app = App("1", "test", "w122", env = mapOf("SENTRY_AUTH_TOKEN" to null))
        Assert.assertEquals("", app.dockerEnvVariables())
    }
}