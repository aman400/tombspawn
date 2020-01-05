package com.tombspawn.docker

sealed class QueueAction

data class QueueAddAction(val id: String, val deferred: (suspend () -> Any?)): QueueAction()
data class QueueProcessCompleteAction(val id: String): QueueAction()
object QueueVerifyAndRunAction : QueueAction()

data class ProcessRequestCommand(val id: String, val deferred: (suspend () -> Any?))