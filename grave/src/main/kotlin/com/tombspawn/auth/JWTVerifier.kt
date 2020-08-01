package com.tombspawn.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.tombspawn.models.config.JWTConfig

fun makeJwtVerifier(jwtConfig: JWTConfig): JWTVerifier = JWT
        .require(jwtConfig.algorithm)
        .withAudience(jwtConfig.audience)
        .withIssuer(jwtConfig.domain)
        .build()