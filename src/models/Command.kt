package com.ramukaka.models

import java.io.File
import java.util.concurrent.TimeUnit

data class Command(val command: String, val workingDir: File = File("."),
                   val timeoutAmount: Long = 15,
                   val timeoutUnit: TimeUnit = TimeUnit.MINUTES)