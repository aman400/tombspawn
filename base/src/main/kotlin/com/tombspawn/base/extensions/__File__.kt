@file:JvmName("FileUtils")

package com.tombspawn.base.extensions

import org.slf4j.LoggerFactory
import java.io.File

private val LOGGER = LoggerFactory.getLogger("com.tombspawn.base.extensions.FileUtils")

fun File.cleanup() {
    if (exists()) {
        if (isFile) {
            delete()
        } else {
            listFiles()?.forEach {
                LOGGER.trace("deleting ${it.name}")
                it.cleanup()
            }
        }
        delete()
    }
}