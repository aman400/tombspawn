package com.tombspawn.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.tombspawn.data.DBUser
import java.util.*
import java.util.concurrent.TimeUnit

class JWTConfig(secret: String, private val issuer: String, private val audience: String, private val validity: Long = 3600 * 1000 * 12,
                private val timeUnit: TimeUnit = TimeUnit.MILLISECONDS) {
    private val algorithm: Algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun createToken(user: DBUser): String = JWT.create()
        .withSubject("Auth")
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("id", user.slackId)
        .withExpiresAt(getExpiration())
        .sign(algorithm)

    fun parseToken(token: String): String? {
        return JWT.decode(token).issuer
    }

    /**
     * Calculate the expiration Date based on current time + the given validity
     */
    private fun getExpiration() = Date(System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(validity, timeUnit))

}