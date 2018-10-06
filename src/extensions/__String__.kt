package com.ramukaka.extensions

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

fun String.runCommand(workingDir: File = File("."),
                      timeoutAmount: Long = 5,
                      timeoutUnit: TimeUnit = TimeUnit.MINUTES): String? {
    return try {
        val strings = split(Regex("\\s+"))
        strings.forEach {
            println(it)
        }
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