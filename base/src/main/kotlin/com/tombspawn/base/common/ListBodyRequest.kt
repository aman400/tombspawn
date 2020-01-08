package com.tombspawn.base.common

import com.google.gson.annotations.SerializedName

data class ListBodyRequest<T> constructor(@SerializedName("data") val data: List<T>)