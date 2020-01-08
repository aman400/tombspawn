package com.tombspawn.skeleton.app

import com.google.gson.JsonObject
import com.tombspawn.base.common.ErrorResponse
import com.tombspawn.base.common.ListBodyRequest
import com.tombspawn.base.common.Response
import com.tombspawn.base.common.SuccessResponse
import com.tombspawn.base.extensions.await
import com.tombspawn.base.network.MultiPartContent
import com.tombspawn.base.network.withRetry
import com.tombspawn.skeleton.di.qualifiers.UploadAppClient
import com.tombspawn.skeleton.models.Reference
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.http.*
import java.io.File
import javax.inject.Inject

class AppClient @Inject constructor(@UploadAppClient private val uploadHttpClient: HttpClient) {
    suspend fun uploadFile(
        url: String,
        apkPrefix: String,
        file: File,
        parameters: Map<String, String>?
    ): Response<JsonObject> {
        return withRetry(3, 5000, 10000, 1.5) {
            uploadHttpClient.call(url) {
                method = HttpMethod.Post
                body = MultiPartContent.build {
                    add("title", apkPrefix)
                    parameters?.forEach { (key, value) ->
                        add(key, value)
                    }
                    add("file", file.readBytes(), filename = file.name)
                }
            }.await<JsonObject>()
        }
    }

    suspend fun initComplete(url: String, success: Boolean): Response<JsonObject> {
        return withRetry(3, 5000, 10000, 1.5) {
            uploadHttpClient.call(url) {
                method = HttpMethod.Post
                body = SuccessResponse(if (success) "success" else "failed")
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json)
            }.await<JsonObject>()
        }
    }

    suspend fun reportFailure(url: String, errorResponse: ErrorResponse): Response<JsonObject> {
        return withRetry(3, 10000, 20000, 1.5) {
            uploadHttpClient.call(url) {
                this.contentType(ContentType.Application.Json)
                method = HttpMethod.Post
                this.body = errorResponse
            }.await<JsonObject>()
        }
    }

    suspend fun sendFlavours(url: String, flavours: List<String>): Response<JsonObject> {
        return withRetry(3, 10000, 20000, 1.5) {
            uploadHttpClient.call(url) {
                method = HttpMethod.Post
                body = ListBodyRequest(flavours)
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json)
            }.await<JsonObject>()
        }
    }

    suspend fun sendBuildVariants(url: String, buildVariants: List<String>): Response<JsonObject> {
        return withRetry(3, 10000, 20000, 1.5) {
            uploadHttpClient.call(url) {
                method = HttpMethod.Post
                body = ListBodyRequest(buildVariants)
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json)
            }.await<JsonObject>()
        }
    }

    suspend fun sendReferences(url: String, refs: List<Reference>): Response<JsonObject> {
        return withRetry(3, 10000, 20000, 1.5) {
            uploadHttpClient.call(url) {
                method = HttpMethod.Post
                body = ListBodyRequest(refs)
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json)
            }.await<JsonObject>()
        }
    }
}