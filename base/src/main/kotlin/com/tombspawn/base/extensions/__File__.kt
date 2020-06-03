package com.tombspawn.base.extensions

import org.apache.commons.io.FileUtils
import java.io.File

fun File.moveToDirectory(destination: File) {
    if(!this.exists()) {
        return
    }
    if (this.isDirectory) {
        val newDest = File(destination, this.name)
        if (!newDest.exists()) {
            newDest.mkdirs()
        }
        this.listFiles()?.forEach {
            it.moveToDirectory(newDest)
        }
        this.delete()
    } else {
        FileUtils.moveFileToDirectory(this, destination, false)
    }
}