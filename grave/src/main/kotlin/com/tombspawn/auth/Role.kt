package com.tombspawn.auth;

enum class Role(val roleStr: String) {
    ADMIN("admin"),
    USER("user");

    override fun toString(): String {
        return roleStr
    }
}