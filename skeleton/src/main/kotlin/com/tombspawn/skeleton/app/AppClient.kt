package com.tombspawn.skeleton.app

import com.google.gson.JsonObject
import com.tombspawn.base.common.ErrorResponse
import com.tombspawn.base.common.Response
import com.tombspawn.base.extensions.await
import com.tombspawn.base.network.MultiPartContent
import com.tombspawn.skeleton.di.qualifiers.UploadAppClient
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import java.io.File
import javax.inject.Inject

class AppClient @Inject constructor(@UploadAppClient
                                    private val uploadHttpClient: HttpClient) {
    suspend fun uploadFile(url: String, apkPrefix: String, file: File, parameters: Map<String, String>?): Response<JsonObject> {
        return uploadHttpClient.call(url) {
            method = HttpMethod.Post
            body = MultiPartContent.build {
                add("title", apkPrefix)
                parameters?.forEach { (key, value) ->
                    add(key, value)
                }
                add("file", file.readBytes(), filename = file.name)
            }
        }.await()
    }

    suspend fun reportFailure(url: String, errorResponse: ErrorResponse): Response<JsonObject> {
        return uploadHttpClient.call(url) {
            this.contentType(ContentType.Application.Json)
            method = HttpMethod.Post
            this.body = errorResponse
        }.await()
    }
}