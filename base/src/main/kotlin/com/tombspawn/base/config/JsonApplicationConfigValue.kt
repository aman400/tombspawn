package com.tombspawn.base.config

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.ktor.config.ApplicationConfigValue
import java.lang.reflect.Type

class JsonApplicationConfigValue(private val gson: Gson, private val config: JsonElement) : ApplicationConfigValue {
    override fun getList(): List<String> {
        return when {
            config.isJsonArray -> config.asJsonArray.filter { it.isJsonPrimitive }.map {
                it.toString()
            }
            config.isJsonObject -> listOf(gson.toJson(config, JsonElement::class.java))
            config.isJsonArray -> listOf(gson.toJson(config, JsonArray::class.java))
            else -> listOf()
        }
    }

    override fun getString(): String {
        return when {
            config.isJsonPrimitive -> config.asString
            config.isJsonObject -> gson.toJson(config, JsonObject::class.java)
            config.isJsonArray -> gson.toJson(config, JsonArray::class.java)
            else -> ""
        }
    }

    fun <T> getAs(clazz: Class<out T>): T {
        return gson.fromJson(config, clazz)
    }

    fun <T> getAs(type: Type): T {
        return gson.fromJson(config, type)
    }
}