package com.tombspawn.skeleton.app

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.reflect.TypeToken
import com.tombspawn.base.common.ErrorResponse
import com.tombspawn.base.common.ListBodyRequest
import com.tombspawn.base.common.Response
import com.tombspawn.base.common.SuccessResponse
import com.tombspawn.base.extensions.await
import com.tombspawn.base.network.MultiPartContent
import com.tombspawn.skeleton.di.qualifiers.UploadAppClient
import com.tombspawn.skeleton.models.Reference
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.header
import io.ktor.http.*
import java.io.File
import javax.inject.Inject

class AppClient @Inject constructor(@UploadAppClient
                                    private val uploadHttpClient: HttpClient,
                                    private val gson: Gson) {
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

    suspend fun initComplete(url: String, success: Boolean): Response<JsonObject> {
        return uploadHttpClient.call(url) {
            method = HttpMethod.Post
            body = SuccessResponse(if(success) "success" else "failed")
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json)
        }.await()
    }

    suspend fun reportFailure(url: String, errorResponse: ErrorResponse): Response<JsonObject> {
        return uploadHttpClient.call(url) {
            this.contentType(ContentType.Application.Json)
            method = HttpMethod.Post
            this.body = errorResponse
        }.await()
    }

    suspend fun sendFlavours(url: String, flavours: List<String>): Response<JsonObject> {
        return uploadHttpClient.call(url) {
            method = HttpMethod.Post
            body = ListBodyRequest(flavours)
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json)
        }.await()
    }

    suspend fun sendBuildVariants(url: String, buildVariants: List<String>): Response<JsonObject> {
        return uploadHttpClient.call(url) {
            method = HttpMethod.Post
            body = ListBodyRequest(buildVariants)
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json)
        }.await()
    }

    suspend fun sendReferences(url: String, refs: List<Reference>): Response<JsonObject> {
        return uploadHttpClient.call(url) {
            method = HttpMethod.Post
            body = ListBodyRequest(refs)
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json)
        }.await()
    }
}