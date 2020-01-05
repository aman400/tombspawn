package com.tombspawn.base

import com.tombspawn.base.common.exhaustive
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import java.util.concurrent.LinkedBlockingQueue

val queues = mutableMapOf<String, LinkedBlockingQueue<(() -> Unit)>>()

val runningProcesses = mutableMapOf<String, Int>()

lateinit var sendChannel: SendChannel<QueueAction>
lateinit var processCommandChannel: SendChannel<ProcessRequestCommand>

sealed class QueueAction

data class QueueAddAction(val id: String, val deferred: (() -> Unit)): QueueAction()
data class QueueProcessStartAction(val id: String): QueueAction()
data class QueueProcessCompleteAction(val id: String): QueueAction()
object QueueVerifyAndRunAction : QueueAction()

data class ProcessRequestCommand(val id: String, val deferred: (() -> Unit))

fun CoroutineScope.processRequest(): SendChannel<ProcessRequestCommand> = actor(capacity = Channel.UNLIMITED) {
    for (msg in channel) {
        GlobalScope.launch(Dispatchers.IO) {
            msg.deferred.invoke()
        }
    }
}

fun CoroutineScope.queueAction(): SendChannel<QueueAction> = actor(capacity = Channel.UNLIMITED) {
    for (msg in channel) {
        when (msg) {
            is QueueAddAction -> {
                println("Adding entry")
                if (queues[msg.id] == null) {
                    queues[msg.id] = LinkedBlockingQueue(100)
                    if (runningProcesses[msg.id] == null) {
                        runningProcesses[msg.id] = 0
                    }
                }
                queues[msg.id]?.offer(msg.deferred)
                println("Size ${queues[msg.id]?.size}")
            }
            is QueueProcessStartAction -> {
                println("Starting next process with id ${msg.id}")
                runningProcesses[msg.id] = (runningProcesses[msg.id] ?: 0) + 1
            }
            is QueueProcessCompleteAction -> {
                println("Process completed with id ${msg.id}")
                runningProcesses[msg.id] = ((runningProcesses[msg.id] ?: 0) - 1).coerceAtLeast(0)
            }
            is QueueVerifyAndRunAction -> {
                if(getRunningTasks() < 2) {
                    val nextEntry = getNextTask()
                    println("NextEntry ${nextEntry?.value?.size}, Running tasks count: ${getRunningTasks()}")
                    val next = nextEntry?.value?.poll()
                    if(next != null) {
                        sendChannel.send(QueueProcessStartAction(nextEntry.key))
                        processCommandChannel.send(ProcessRequestCommand(nextEntry.key, next))
                    } else {
                        println("Waiting for next queue execution")
                    }
                } else {
                    println("Waiting for next queue execution")
                }
            }
        }.exhaustive
    }
}


public fun main() {
    runBlocking {
        sendChannel = queueAction()
        processCommandChannel = processRequest()

        launch(Dispatchers.IO) {
            startQueueExecution()
        }
        delay(5000)
        addToQueue("lazysocket", "a")
        delay(1000)
        addToQueue("consumer", "b")
        delay(1000)
        addToQueue("lazysocket", "c")
        delay(3000)
        addToQueue("fleet", "d")
        delay(10000)
        addToQueue("fleet", "e")
        delay(5000)
        addToQueue("lazysocket", "f")
        delay(2000)
        addToQueue("fleet", "g")
        delay(1000)
        addToQueue("lazysocket", "h")
        delay(300)
        addToQueue("consumer", "i")
        delay(3000)
        addToQueue("consumer", "j")
        addToQueue("fleet", "k")
        addToQueue("lazysocket", "l")

    }
}



suspend fun startQueueExecution() {
    while(true) {
        sendChannel.offer(QueueVerifyAndRunAction)
        delay(500)
    }
}

suspend fun addToQueue(id: String, element: String) {
    GlobalScope.launch(Dispatchers.IO) {

        sendChannel.offer(QueueAddAction(id) {
            runBlocking {
                delay(10000)
                println(element)
                sendChannel.offer(QueueProcessCompleteAction(id))
            }
        })
    }
}

fun getNextTask(): Map.Entry<String, LinkedBlockingQueue<() -> Unit>>? {
    return queues.filter {
        it.value.size > 0
    }.filter {
        (runningProcesses[it.key] ?: 0) <= 0
    }.entries.firstOrNull()
}

fun getRunningTasks(): Int {
    return runningProcesses.map {
        it.value
    }.takeIf { it.isNotEmpty() }?.reduce { acc, i -> acc + i } ?: 0
}