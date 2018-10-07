package com.ramukaka.network

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
}