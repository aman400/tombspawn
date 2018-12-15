package com.ramukaka.network

sealed class RetrofitResponse <out T>

data class Success <out T> constructor(val data: T?): RetrofitResponse<T>()

data class Failure constructor(val errorBody: String?, val throwable: Throwable? = null): RetrofitResponse<Nothing>()

data class CallError constructor(val throwable: Throwable?): RetrofitResponse<Nothing>()