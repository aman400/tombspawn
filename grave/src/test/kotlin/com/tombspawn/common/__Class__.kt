package com.tombspawn.common

import java.io.File

fun <T> Class<T>.getResourceFile(name: String): File {
    val resource = classLoader.getResource(name)
    if (resource == null) {
        throw IllegalArgumentException("$name is not found!")
    } else {
        return File(resource.file)
    }
}