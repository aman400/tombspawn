package com.ramukaka.auth

import io.ktor.sessions.SessionStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.ByteWriteChannel
import kotlinx.coroutines.io.readAvailable
import kotlinx.coroutines.io.reader
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

abstract class SimplifiedSessionStorage : SessionStorage {
    abstract suspend fun read(id: String): ByteArray?
    abstract suspend fun write(id: String, data: ByteArray?): Unit

    override suspend fun invalidate(id: String) {
        write(id, null)
    }

    override suspend fun <R> read(id: String, consumer: suspend (ByteReadChannel) -> R): R {
        val data = read(id) ?: throw NoSuchElementException("Session $id not found")
        return consumer(ByteReadChannel(data))
    }

    override suspend fun write(id: String, provider: suspend (ByteWriteChannel) -> Unit) =
        coroutineScope {
            provider(reader(coroutineContext, autoFlush = true) {
                write(id, channel.readAvailable())
            }.channel)
        }
}

suspend fun ByteReadChannel.readAvailable(): ByteArray? = coroutineScope {
    withContext(Dispatchers.IO) {
        val data = ByteArrayOutputStream()
        val temp = ByteArray(1024)
        while (!isClosedForRead) {
            val read = readAvailable(temp)
            if (read <= 0) break
            data.write(temp, 0, read)
        }
        data.toByteArray()
    }
}