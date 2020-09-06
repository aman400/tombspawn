package com.tombspawn.models.config

import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.annotations.SerializedName

data class JWTConfig(
    @SerializedName("domain") val domain: String,
    @SerializedName("audience") val audience: String,
    @SerializedName("realm") val realm: String,
    @SerializedName("secret") val secret: String
) {
    val algorithm: Algorithm
        get() = Algorithm.HMAC256(secret)
}