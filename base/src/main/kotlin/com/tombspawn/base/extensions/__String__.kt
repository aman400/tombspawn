package com.tombspawn.base.extensions

import org.slf4j.LoggerFactory
import java.security.MessageDigest

private val LOGGER = LoggerFactory.getLogger("com.application.StringUtils")

fun String.toMap(): MutableMap<String, String>? {
    val returnValue = mutableMapOf<String, String>()
    if (isNotEmpty()) {
        val params = split(Regex("\\s+"))
        params.forEach { param ->
            val pair = param.split(Regex("="))
            if (pair.size == 2) {
                returnValue[pair[0]] = pair[1]
            } else {
                LOGGER.error("Invalid parameters. $this")
                return null
            }
        }
    } else {
        return null
    }
    return returnValue
}