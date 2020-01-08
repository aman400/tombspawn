package com.tombspawn.base.common

sealed class Response <out T>

data class CallSuccess <out T> constructor(val data: T?): Response<T>()

data class CallFailure constructor(val errorBody: String?, val throwable: Throwable? = null, val errorCode: Int? = null): Response<Nothing>()

data class ServerFailure constructor(val throwable: Throwable? = null, val errorCode: Int? = null, val errorBody: String? = null): Response<Nothing>()

data class CallError constructor(val throwable: Throwable?): Response<Nothing>()

val <T> T.exhaustive: T
    get() = this