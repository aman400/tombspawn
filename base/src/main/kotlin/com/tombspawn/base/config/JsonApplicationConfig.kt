package com.tombspawn.base.config

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import io.ktor.config.ApplicationConfig
import io.ktor.config.ApplicationConfigValue

class JsonApplicationConfig: ApplicationConfig {
    private val gson: Gson
    private val config: JsonElement
    constructor(gson: Gson, jsonConfig: JsonElement) {
        this.gson = gson
        this.config = jsonConfig
    }
    constructor(gson: Gson, config: String) {
        this.gson = gson
        this.config = gson.fromJson(config, JsonElement::class.java)
    }

    override fun config(path: String): JsonApplicationConfig {
        return if(config.isJsonObject && config.asJsonObject.has(path)) {
            JsonApplicationConfig(gson, config.asJsonObject.get(path))
        } else {
            JsonApplicationConfig(gson, JsonObject())
        }
    }

    override fun configList(path: String): List<JsonApplicationConfig> {
        return if(config.isJsonObject && config.asJsonObject.has(path) && config.asJsonObject.get(path).isJsonArray) {
            config.asJsonObject.get(path).asJsonArray.map {
                JsonApplicationConfig(gson, it)
            }
        } else {
            listOf(JsonApplicationConfig(gson, config))
        }
    }

    override fun property(path: String): JsonApplicationConfigValue {
        return if(config.isJsonObject && config.asJsonObject.has(path)) {
            JsonApplicationConfigValue(gson, config.asJsonObject.get(path))
        } else {
            JsonApplicationConfigValue(gson, JsonObject())
        }
    }

    override fun propertyOrNull(path: String): JsonApplicationConfigValue? {
        return if(config.isJsonObject && config.asJsonObject.has(path)) {
            JsonApplicationConfigValue(gson, config.asJsonObject.get(path))
        } else {
            null
        }
    }

    fun <T> getAs(clazz: Class<T>): T {
        return gson.fromJson(config, clazz)
    }

    fun <T> getAs(typeToken: TypeToken<T>): T? {
        return gson.fromJson(config, typeToken.type)
    }
}