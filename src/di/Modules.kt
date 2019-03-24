package com.ramukaka.di

import com.ramukaka.data.Database
import io.ktor.client.HttpClient
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import org.koin.dsl.module
import annotations.DoNotSerialize
import com.google.gson.FieldAttributes
import com.google.gson.ExclusionStrategy
import annotations.DoNotDeserialize
import io.ktor.client.engine.apache.Apache


val dbModule = module {
    single {
        val dbUrl = System.getenv()["DB_URL"]!!
        val dbUser = System.getenv()["DB_USER"]!!
        val dbPassword = System.getenv()["DB_PASSWORD"]!!
        Database(dbUrl, dbUser, dbPassword)
    }
}

val httpClienModule = module {
    single {
        HttpClient(Apache) {
            install(JsonFeature) {
                serializer = GsonSerializer {
                    serializeNulls()
                    disableHtmlEscaping()
                    setPrettyPrinting()
                    enableComplexMapKeySerialization()
                    addSerializationExclusionStrategy(object : ExclusionStrategy {
                        override fun shouldSkipField(f: FieldAttributes): Boolean {
                            return f.getAnnotation(DoNotSerialize::class.java) != null
                        }

                        override fun shouldSkipClass(clazz: Class<*>): Boolean {
                            return clazz.getAnnotation(DoNotSerialize::class.java) != null
                        }
                    })
                    addDeserializationExclusionStrategy(object : ExclusionStrategy {
                        override fun shouldSkipField(f: FieldAttributes): Boolean {
                            return f.getAnnotation(DoNotDeserialize::class.java) != null
                        }

                        override fun shouldSkipClass(clazz: Class<*>): Boolean {
                            return clazz.getAnnotation(DoNotDeserialize::class.java) != null
                        }
                    })
                }
            }
        }
    }
}