package com.ramukaka.extensions

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

fun String.execute(workingDir: File = File("."),
                   timeoutAmount: Long = 5,
                   timeoutUnit: TimeUnit = TimeUnit.MINUTES): String? {
    return try {
        val strings = split(Regex("\\s+"))
        ProcessBuilder(strings)
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start().apply {
                waitFor(timeoutAmount, timeoutUnit)
            }.inputStream.bufferedReader().readText()
    } catch (exception: IOException) {
        exception.printStackTrace()
        null
    }
}

fun String.toMap(): MutableMap<String, String> {
    val returnValue = mutableMapOf<String, String>()
    val params = split(Regex("\\s+"))
    val data = params.forEach {
        val pair = it.split(Regex("="))
        returnValue[pair[0]] = pair[1]
    }

    return returnValue
}