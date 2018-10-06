package com.ramukaka.extensions

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

fun String.runCommand(workingDir: File = File("."),
                      timeoutAmount: Long = 5,
                      timeoutUnit: TimeUnit = TimeUnit.MINUTES): String? {
    return try {
//        val strings = this.split(" &&".toRegex()).toTypedArray()
        val strings = arrayOf("./build.sh")
        ProcessBuilder(*strings)
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start().apply {
                waitFor(timeoutAmount, timeoutUnit)
            }.inputStream.bufferedReader().readText()
    } catch (exception: IOException) {
        exception.printStackTrace()
        null
    }
}