package com.tombspawn.skeleton.app

import com.google.gson.JsonObject
import com.tombspawn.base.common.*
import com.tombspawn.base.extensions.await
import com.tombspawn.base.network.withRetry
import com.tombspawn.skeleton.di.qualifiers.UploadAppClient
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import javax.inject.Inject

class AppClient @Inject constructor(@UploadAppClient private val uploadHttpClient: HttpClient) {
    suspend fun initComplete(url: String, success: Boolean): Response<JsonObject> {
        return withRetry(3, 5000, 10000, 1.5) {
            uploadHttpClient.request<HttpResponse>(url) {
                method = HttpMethod.Post
                body = SuccessResponse(if (success) "success" else "failed")
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json)
            }.await<JsonObject>()
        }
    }
}