package com.ramukaka.extensions

import java.util.logging.Logger

private val LOGGER = Logger.getLogger("com.application.StringUtils")

fun String.toMap(): MutableMap<String, String>? {
    val returnValue = mutableMapOf<String, String>()
    if (isNotEmpty()) {
        val params = split(Regex("\\s+"))
        params.forEach { param ->
            val pair = param.split(Regex("="))
            if (pair.size == 2) {
                returnValue[pair[0]] = pair[1]
            } else {
                LOGGER.severe("Invalid parameters. $this")
                return null
            }
        }
    }
    return returnValue

}