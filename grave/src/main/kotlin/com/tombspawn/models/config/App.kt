package com.tombspawn.models.config

import com.google.gson.annotations.SerializedName
import com.tombspawn.base.annotations.DoNotDeserialize
import com.tombspawn.base.annotations.DoNotSerialize
import com.tombspawn.base.common.CallError
import com.tombspawn.base.common.CallFailure
import com.tombspawn.base.common.CallSuccess
import com.tombspawn.base.common.exhaustive
import com.tombspawn.base.extensions.await
import com.tombspawn.models.Reference
import com.tombspawn.network.GradleExecutor
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import io.ktor.http.parametersOf
import io.ktor.response.respond

data class App constructor(@SerializedName("id") val id: String,
               @SerializedName("name") val name: String?,
               @SerializedName("app_url") val appUrl: String?,
               @SerializedName("repo_id") val repoId: String?,
               @SerializedName("dir") var dir: String? = null,
               @SerializedName("remote_uri") val uri: String? = null) {
//    @DoNotSerialize
//    @DoNotDeserialize
//    var gradleExecutor: GradleExecutor? = null
}