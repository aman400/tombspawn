package com.ramukaka.network

import com.google.gson.JsonObject
import com.ramukaka.models.ErrorResponse
import com.ramukaka.models.Response
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface RamukakaApi {

    @Multipart
    @POST("/api/files.upload")
    fun pushApp(
        @Part("token") token: RequestBody,
        @Part("title") title: RequestBody,
        @Part("filename") filename: RequestBody,
        @Part("filetype") filetype: RequestBody,
        @Part("channels") channels: RequestBody,
        @Part body: MultipartBody.Part
    ): Call<Response>

    @POST
    fun sendError(@HeaderMap header: MutableMap<String, String>, @Url url: String,
                  @Body errorResponse: ErrorResponse) : Call<String>

    @FormUrlEncoded
    @POST("api/chat.postMessage")
    fun postAction(@HeaderMap headers: MutableMap<String, String>, @FieldMap body: MutableMap<String, String?> ): Call<JsonObject>
}