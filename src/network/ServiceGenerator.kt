package com.ramukaka.network

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapterFactory
import com.ramukaka.network.interceptors.GzipRequestInterceptor
import com.ramukaka.network.interceptors.HeadersInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ServiceGenerator {
    companion object {
        val MULTIPART_FORM_DATA = "multipart/form-data"
        private val CACHE_SIZE = 10 * 1024 * 1024 // 10 MiB
        private val CACHE_DIRECTORY = "Lazy Socket cache"

        private val retrofitBuilder = Retrofit.Builder()

        fun <T> createService(
            serviceClass: Class<T>, url: String, isLoggingEnabled: Boolean = false, isGzipEnabled: Boolean = false,
            cache: Cache? = null, headers: MutableMap<String, String>? = null,
            typeAdapterFactory: TypeAdapterFactory? = null, callAdapterFactory: CallAdapter.Factory?= null
        ): T {

            retrofitBuilder.baseUrl(url)
            if(callAdapterFactory != null) {
                retrofitBuilder.addCallAdapterFactory(callAdapterFactory)
            }

            val gsonBuilder = GsonBuilder().setLenient().setPrettyPrinting()


            var okHttpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
                .addInterceptor(HeadersInterceptor(headers))

            if (cache != null) {
                okHttpClientBuilder = okHttpClientBuilder.cache(cache)
            }

            if (isLoggingEnabled) {
                val loggingInterceptor = HttpLoggingInterceptor()
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                okHttpClientBuilder.addInterceptor(loggingInterceptor)
            }

            if (isGzipEnabled) {
                okHttpClientBuilder.addInterceptor(GzipRequestInterceptor())
            }

            retrofitBuilder.client(okHttpClientBuilder.build())
            if (typeAdapterFactory != null) {
                gsonBuilder.registerTypeAdapterFactory(typeAdapterFactory)
            }
            retrofitBuilder.addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
            return retrofitBuilder.build().create(serviceClass)
        }
    }
}