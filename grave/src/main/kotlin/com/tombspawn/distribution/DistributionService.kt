package com.tombspawn.distribution

import com.google.protobuf.ByteString
import com.tombspawn.ApplicationService
import com.tombspawn.base.common.*
import com.tombspawn.base.common.CommonConstants.COMMIT_MESSAGE
import com.tombspawn.base.common.models.GradleTask
import com.tombspawn.base.di.scopes.AppScope
import com.tombspawn.base.distribution.DistributionGrpc
import com.tombspawn.base.distribution.UploadRequest
import com.tombspawn.base.distribution.UploadResponse
import com.tombspawn.base.distribution.UploadResponseOrBuilder
import com.tombspawn.base.network.Common
import com.tombspawn.models.config.Distribution
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AppScope
class DistributionService @Inject constructor(
    val distribution: Distribution
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun distributeApp(
        distrib: GradleTask.Distribution.Firebase,
        file: ByteArray,
        releaseNotes: String,
        testers: String,
        fileName: String
    ) = callbackFlow<Response<UploadResponse>> {
        with(
            Common.createGrpcChannel(
                distribution.host,
                distribution.port
            )
        ) {
            DistributionGrpc.newStub(this)
                .withDeadlineAfter(45, TimeUnit.MINUTES)
                .app(object : StreamObserver<UploadResponse> {
                    override fun onNext(value: UploadResponse?) {
                        sendBlocking(CallSuccess(value))
                    }

                    override fun onError(t: Throwable?) {
                        sendBlocking(CallError(t))
                    }

                    override fun onCompleted() {
                        close()
                    }
                }).apply {
                    try {
                        this.onNext(
                            UploadRequest.newBuilder()
                                .setAppId(distrib.appId)
                                .setToken(distrib.token)
                                .setTesters(testers)
                                .setReleaseNotes(releaseNotes)
                                .setFileName(fileName)
                                .build()
                        )
                        file.toList().chunked(500 * 1025).forEach {
                            this.onNext(
                                UploadRequest.newBuilder()
                                    .setFile(ByteString.copyFrom(it.toByteArray()))
                                    .build()
                            )
                        }
                        this.onCompleted()
                    } catch (exception: Exception) {
                        this.onError(exception)
                    }
                }
            awaitClose {
                this@callbackFlow.cancel()
            }
        }
    }
}

suspend fun ApplicationService.distribute(
    gradleTask: GradleTask, file: ByteArray,
    map: MutableMap<String, String>,
    channelId: String, fileName: String,
    onCompleted: (completed: Boolean) -> Unit
) = withContext(Dispatchers.IO) {
    gradleTask.distribution?.let { distribution ->
        distributionService.distributeApp(
            distribution.firebase,
            file,
            map.getOrDefault(COMMIT_MESSAGE, "new update"),
            "amandeep400@gmail.com",
            fileName
        ).collect {
            when (it) {
                is CallSuccess -> {
                    if (it.data?.success == true) {
                        slackService.sendMessage(it.data?.message ?: "App distributed successfully", channelId, null)
                        onCompleted(true)
                    } else {
                        slackService.sendMessage(it.data?.message ?: "Unable to distribute app", channelId, null)
                        onCompleted(false)
                    }
                }
                is CallFailure -> {
                    slackService.sendMessage("Unable to distribute app", channelId, null)
                    onCompleted(false)
                }
                is ServerFailure -> {
                    slackService.sendMessage("Unable to distribute app", channelId, null)
                    onCompleted(false)
                }
                is CallError -> {
                    slackService.sendMessage(it.toString(), channelId, null)
                    onCompleted(false)
                }
            }
        }
    } ?: run {
        slackService.sendMessage("Unable to distribute app", channelId, null)
        onCompleted(false)
    }

}