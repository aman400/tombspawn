package com.tombspawn.session

import com.google.gson.Gson
import io.ktor.sessions.SessionSerializer

class GsonSessionSerializer<T>(
    val type: java.lang.reflect.Type,
    val gson: Gson = Gson(),
    configure: Gson.() -> Unit = {}
) : SessionSerializer<T> {
    init {
        configure(gson)
    }

    override fun serialize(session: T): String {
        return gson.toJson(session, type)
    }


    override fun deserialize(text: String): T {
        return gson.fromJson(text, type)
    }
}