package com.ramukaka.network

import com.ramukaka.models.slack.SlackProfileResponse
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface SlackApi {
    companion object {
        const val BASE_URL = "https://slack.com"
        const val PARAM_TOKEN = "token"
        const val PARAM_USER_ID = "user"
    }

    @GET("/api/users.profile.get")
    fun getProfile (@QueryMap queryMap: MutableMap<String, String>): Observable<Response<SlackProfileResponse>>
}