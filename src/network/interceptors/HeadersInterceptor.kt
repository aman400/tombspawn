package com.ramukaka.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException


class HeadersInterceptor(private var headers: Map<String, String>?) : Interceptor {


    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        val originalRequest = chain.request()

        val newRequestBuilder = originalRequest.newBuilder()
        if (headers != null) {
            for ((key, value) in headers!!) {
                newRequestBuilder.addHeader(key, value)
            }
        }
        return chain.proceed(newRequestBuilder.build())
    }
}