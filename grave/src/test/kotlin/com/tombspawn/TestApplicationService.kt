package com.tombspawn

import com.google.common.base.Optional
import com.google.gson.Gson
import com.tombspawn.base.common.SlackConstants
import com.tombspawn.common.anyObject
import com.tombspawn.data.CachingService
import com.tombspawn.data.DatabaseService
import com.tombspawn.data.cache.models.ApkCache
import com.tombspawn.di.DaggerFakeCachingComponent
import com.tombspawn.di.DaggerFakeCoreComponent
import com.tombspawn.docker.DockerService
import com.tombspawn.models.config.App
import com.tombspawn.models.config.Common
import com.tombspawn.models.config.ServerConf
import com.tombspawn.models.config.Slack
import com.tombspawn.slackbot.SlackService
import com.tombspawn.utils.Constants
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito
import org.mockito.Mockito.mock
import javax.inject.Inject
import javax.inject.Provider

class TestApplicationService {
    lateinit var applicationService: ApplicationService

    @Inject
    lateinit var gson: Gson

    lateinit var cachingService: CachingService

    @get:Rule var tempFolder = TemporaryFolder()

    lateinit var slackService: SlackService

    @Before
    fun setup() {
        val component = DaggerFakeCoreComponent.create()
        DaggerFakeCachingComponent.factory().create(component)
            .inject(this)
        val slack = mock(Slack::class.java)
        val common = mock(Common::class.java)
        val databaseService = mock(DatabaseService::class.java)
        val dockerService = mock(DockerService::class.java)
        cachingService = mock(CachingService::class.java)
        slackService = mock(SlackService::class.java)

        applicationService = ApplicationService(slack, common, gson, databaseService,
            listOf(App("test", "test", "1")), dockerService,
            slackService, tempFolder.newFolder("apks"), cachingService, true,
            Optional.of(ServerConf()), Provider {
                URLBuilder().apply {
                    this.protocol = URLProtocol.HTTP
                    this.host = Constants.Common.DEFAULT_HOST
                    this.port = Constants.Common.DEFAULT_PORT
                }
            }, Optional.fromNullable(null)
        )
    }

    @Test
    fun testCache() {
        runBlocking {
            val buildData = mutableMapOf(SlackConstants.TYPE_SELECT_BRANCH to "b", "c" to "d")
            Mockito.`when`(slackService.uploadFile(anyObject(), anyObject(), anyObject(), anyObject())).then {
                @Suppress("UNCHECKED_CAST")
                (it.arguments[3] as? (() -> Unit))?.invoke()
            }
            tempFolder.newFile("test.txt")?.apply {
                this.writer().write("This is a test string")
            }?.let { file ->
                Mockito.`when`(cachingService.cacheApk(Mockito.anyString(), Mockito.anyString(), anyObject())).then {
                    (it.arguments[2] as ApkCache).pathOnDisk.let {
                        Assert.assertEquals("File paths are not equal", file.absolutePath, it)
                    }
                }
                applicationService.uploadApk(file.readBytes(), buildData, "1", "app.apk", null)
            }
        }
    }
}