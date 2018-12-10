package com.ramukaka.network

sealed class RetrofitResponse <out T>

data class Success <out T> constructor(val data: T?): RetrofitResponse<T>()

data class Failure <out T> constructor(val errorBody: String?, val throwable: Throwable? = null): RetrofitResponse<T>()

data class CallError <out T> constructor(val throwable: Throwable?): RetrofitResponse<T>()