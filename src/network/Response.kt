package com.ramukaka.network

sealed class Response <out T>

data class Success <out T> constructor(val data: T?): Response<T>()

data class Failure constructor(val errorBody: String?, val throwable: Throwable? = null): Response<Nothing>()

data class CallError constructor(val throwable: Throwable?): Response<Nothing>()

val <T> T.exhaustive: T
    get() = this