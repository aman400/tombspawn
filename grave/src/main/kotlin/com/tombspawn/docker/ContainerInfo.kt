package com.tombspawn.docker

import kotlinx.coroutines.delay
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock

data class ContainerInfo constructor(
    val appId: String, var containerId: String? = null, private var status: ContainerState = ContainerState.INVALID,
    private var runningTaskQueue: LinkedBlockingQueue<suspend () -> Any?> = LinkedBlockingQueue(100),
    private var pendingTaskQueue: LinkedBlockingQueue<suspend () -> Any?> = LinkedBlockingQueue(100)
) {
    private val TASK_QUEUE_LOCK = ReentrantLock()
    private val CONTAINER_STATE_LOCK = ReentrantLock()

    fun getRunningTaskCount(): Int {
        return if(TASK_QUEUE_LOCK.tryLock()) {
            try {
                runningTaskQueue.size
            } finally {
                TASK_QUEUE_LOCK.unlock()
            }
        } else {
            0
        }
    }

    fun canRunTasks(): Boolean {
        return if (TASK_QUEUE_LOCK.tryLock() && CONTAINER_STATE_LOCK.tryLock()) {
            try {
                status == ContainerState.STARTED && runningTaskQueue.size == 0 && pendingTaskQueue.size > 0
            } finally {
                TASK_QUEUE_LOCK.unlock()
                CONTAINER_STATE_LOCK.unlock()
            }
        } else {
            false
        }
    }

    suspend fun setContainerState(state: ContainerState) {
        while (true) {
            if (!CONTAINER_STATE_LOCK.isLocked) {
                try {
                    CONTAINER_STATE_LOCK.lock()
                    this.status = state
                    break
                } finally {
                    CONTAINER_STATE_LOCK.unlock()
                }
            }
            delay(10)
        }
    }

    suspend fun addNewTask(task: (suspend () -> Any?)) {
        while (true) {
            if (!TASK_QUEUE_LOCK.isLocked) {
                try {
                    TASK_QUEUE_LOCK.lock()
                    pendingTaskQueue.offer(task)
                    break
                } finally {
                    TASK_QUEUE_LOCK.unlock()
                }
            }
            delay(10)
        }
    }

    fun getNextTask(): (suspend () -> Any?)? {
        return if (TASK_QUEUE_LOCK.tryLock()) {
            try {
                val task = pendingTaskQueue.poll()
                runningTaskQueue.offer(task)
                return task
            } finally {
                TASK_QUEUE_LOCK.unlock()
            }
        } else {
            null
        }
    }

    suspend fun resetTasks() {
        while (true) {
            if (!TASK_QUEUE_LOCK.isLocked) {
                try {
                    TASK_QUEUE_LOCK.lock()
                    while(runningTaskQueue.peek() != null) {
                        val runningTask = runningTaskQueue.poll()
                        pendingTaskQueue.offer(runningTask)
                    }
                    break
                } finally {
                    TASK_QUEUE_LOCK.unlock()
                }
            }
            delay(10)
        }
    }

    suspend fun onTaskCompleted() {
        while (true) {
            if (!TASK_QUEUE_LOCK.isLocked) {
                try {
                    TASK_QUEUE_LOCK.lock()
                    runningTaskQueue.poll()
                    break
                } finally {
                    TASK_QUEUE_LOCK.unlock()
                }
            }
            delay(10)
        }
    }
}