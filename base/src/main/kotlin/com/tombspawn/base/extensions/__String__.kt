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

fun String.hash(type: String): String {
    val HEX_CHARS = "0123456789ABCDEF"
    val bytes = MessageDigest
        .getInstance(type)
        .digest(this.toByteArray())
    val result = StringBuilder(bytes.size * 2)

    bytes.forEach {
        val i = it.toInt()
        result.append(HEX_CHARS[i shr 4 and 0x0f])
        result.append(HEX_CHARS[i and 0x0f])
    }

    return result.toString()
}