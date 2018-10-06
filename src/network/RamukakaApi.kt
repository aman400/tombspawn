package com.ramukaka.network

import com.ramukaka.models.Response
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface RamukakaApi {

    @GET("/get/{id}")
    fun get(@Path("id") appId: Int): Call<String>

    @Multipart
    @POST("/app")
    fun pushApp(@Part("description") description: RequestBody, @Part body: MultipartBody.Part) : Call<Response>
}