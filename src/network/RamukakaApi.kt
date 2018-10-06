package com.ramukaka.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Path

interface RamukakaApi {

    @GET("/get/{id}")
    fun get(@Path("id") appId: Int): Call<String>

    @Multipart
    @POST("/app/build")
    fun sendApp() : Call<String>
}