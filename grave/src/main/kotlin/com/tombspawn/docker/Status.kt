package com.tombspawn.docker

enum class Status(val value: String) {
    // Not a docker status
    INVALID("invalid"),

    ATTACH("attach"),
    COMMIT("commit"),
    COPY("copy"),
    CREATE("create"),
    DESTROY("destroy"),
    DETACH("detach"),
    DIE("die"),
    EXEC_CREATE("exec_create"),
    EXEC_DETACH("exec_detach"),
    EXEC_DIE("exec_die"),
    EXEC_START("exec_start"),
    EXPORT("export"),
    HEALTH_STATUS("health_status"),
    KILL("kill"),
    OOM("oom"),
    PAUSE("pause"),
    RENAME("rename"),
    RESIZE("resize"),
    RESTART("restart"),
    START("start"),
    STOP("stop"),
    TOP("top"),
    UNPAUSE("unpause"),
    UPDATE("update");

    companion object {
        @JvmStatic
        fun from(key: String?): Status {
            return values().firstOrNull {
                key?.equals(it.value, true) == true
            } ?: INVALID
        }
    }
}