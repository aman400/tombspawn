package com.ramukaka.extensions

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

fun String.execute(
    workingDir: File = File("."),
    timeoutAmount: Long = 60,
    timeoutUnit: TimeUnit = TimeUnit.MINUTES
): String? {
     try {
        val strings = split(Regex("\\s+"))
        val builder = ProcessBuilder(strings)
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
         val response = builder.inputStream.bufferedReader().readText()
         val errorText = builder.errorStream.bufferedReader().readText()
         builder.apply {
             waitFor(timeoutAmount, timeoutUnit)
         }
         println(response)

         return if(errorText.isEmpty()) response else errorText

    } catch (exception: IOException) {
        exception.printStackTrace()
        return null
    }
}

fun String.toMap(): MutableMap<String, String>? {
    val returnValue = mutableMapOf<String, String>()
    if (isNotEmpty()) {
        val params = split(Regex("\\s+"))
        params.forEach { param ->
            val pair = param.split(Regex("="))
            if (pair.size == 2) {
                returnValue[pair[0]] = pair[1]
            } else {
                println("Invalid parameters. $this")
                return null
            }
        }
    }
    return returnValue

}