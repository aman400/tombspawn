package com.tombspawn.base.config

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import io.ktor.config.ApplicationConfig

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
        return config.findElementInPath(path)?.let {
            JsonApplicationConfig(gson, it)
        } ?: JsonApplicationConfig(gson, JsonObject())
    }

    override fun configList(path: String): List<JsonApplicationConfig> {
        return config.findElementInPath(path)?.takeIf {
            it.isJsonArray
        }?.let {
            it.asJsonArray.map {
                JsonApplicationConfig(gson, it)
            }
        } ?: listOf(JsonApplicationConfig(gson, config))
    }

    override fun property(path: String): JsonApplicationConfigValue {
        return config.findElementInPath(path)?.let {
            JsonApplicationConfigValue(gson, it)
        } ?: JsonApplicationConfigValue(gson, JsonObject())
    }

    override fun propertyOrNull(path: String): JsonApplicationConfigValue? {
        return config.findElementInPath(path)?.let {
            JsonApplicationConfigValue(gson, it)
        }
    }

    fun <T> getAs(clazz: Class<T>): T {
        return gson.fromJson(config, clazz)
    }

    fun <T> getAs(typeToken: TypeToken<T>): T? {
        return gson.fromJson(config, typeToken.type)
    }
}

private fun JsonElement.findElementInPath(path: String): JsonElement? {
    var pointer = this
    val pathIterator = path.split(".").iterator()
    while(pathIterator.hasNext()) {
        val nextPath = pathIterator.next().trim()
        if(pointer.isJsonObject && pointer.asJsonObject.has(nextPath)) {
            pointer = pointer.asJsonObject.get(nextPath)
        } else {
            return null
        }
    }
    return pointer
}