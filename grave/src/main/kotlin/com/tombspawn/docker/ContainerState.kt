package com.tombspawn.docker

enum class ContainerState(val value: String) {
    INVALID("invalid"),

    STARTED("running"),
    CREATED("created"),
    RESTARTING("restarting"),
    PAUSED("paused"),
    EXITED("exited"),
    DEAD("dead");

    companion object {
        @JvmStatic
        fun from(key: String?): ContainerState {
            return values().firstOrNull {
                key?.equals(it.value, true) == true
            } ?: INVALID
        }
    }
}