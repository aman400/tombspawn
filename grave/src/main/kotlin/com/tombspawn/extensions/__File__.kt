package com.tombspawn.extensions

import java.io.File

fun File.cleanup() {
    if (exists()) {
        if (isFile) {
            delete()
        } else {
            listFiles()?.forEach {
                println(it.name)
                it.cleanup()
            }
        }
        delete()
    }
}