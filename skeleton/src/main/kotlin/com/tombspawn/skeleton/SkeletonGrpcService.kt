package com.tombspawn.skeleton

import com.google.protobuf.ByteString
import com.tombspawn.base.*
import com.tombspawn.base.common.Failure
import com.tombspawn.base.common.Success
import com.tombspawn.base.common.exhaustive
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class SkeletonGrpcService(private val applicationService: ApplicationService) : ApplicationGrpc.ApplicationImplBase() {

    private val LOGGER = LoggerFactory.getLogger("com.tombspawn.skeleton.SkeletonGrpcService")

    @ExperimentalStdlibApi
    override fun generateApp(request: GenerateAppRequest?, responseObserver: StreamObserver<GenerateAppResponse>?) {
        request?.buildParamsMap?.let { params ->
            runBlocking {
                applicationService.generateApplication(params.toMutableMap(), { file, params ->
                    file.readBytes().toList().chunked(500 * 1024).forEach { chunk ->
                        LOGGER.debug("Uploading next chunk")
                        responseObserver?.onNext(
                            GenerateAppResponse.newBuilder()
                                .setData(ByteString.copyFrom(chunk.toByteArray()))
                                .build()
                        )
                    }
                    responseObserver?.onNext(
                        GenerateAppResponse.newBuilder()
                            .setFileName(file.name)
                            .putAllResponseParams(params)
                            .build()
                    )
                    responseObserver?.onCompleted()
                }, { message, throwable ->
                    val exception = StatusRuntimeException(
                        Status.UNKNOWN
                            .withDescription(message)
                            .withCause(throwable)
                    )
                    responseObserver?.onError(exception)
                })
            }
        }
    }

    override fun fetchReferences(request: ReferencesRequest?, responseObserver: StreamObserver<ReferencesResponse>?) {
        val branchLimit: Int = request?.branchLimit ?: -1
        val tagLimit: Int = request?.tagLimit ?: -1
        runBlocking {
            applicationService.getReferences(branchLimit, tagLimit).let { refs ->
                responseObserver?.onNext(ReferencesResponse.newBuilder().addAllRef(refs).build())
                responseObserver?.onCompleted()
            }
        }
    }

    override fun clean(request: CleanRequest?, responseObserver: StreamObserver<CleanResponse>?) {
        runBlocking {
            when (val response = applicationService.cleanCode()) {
                is Success -> {
                    responseObserver?.onNext(CleanResponse.newBuilder().build())
                    responseObserver?.onCompleted()
                }
                is Failure -> {
                    responseObserver?.onError(response.throwable)
                    responseObserver?.onCompleted()
                }
            }.exhaustive
        }
    }
}