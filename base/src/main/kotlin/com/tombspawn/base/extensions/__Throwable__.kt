package com.tombspawn.base.extensions

import java.io.PrintWriter

import java.io.StringWriter


fun Throwable?.asString(): String {
    val stringWriter = StringWriter()
    val printWriter = PrintWriter(stringWriter)
    this?.printStackTrace(printWriter)
    return stringWriter.toString()
}