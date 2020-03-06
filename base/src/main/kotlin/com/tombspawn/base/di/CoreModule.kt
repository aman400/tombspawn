package com.tombspawn.base.di

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tombspawn.base.annotations.DoNotDeserialize
import com.tombspawn.base.annotations.DoNotSerialize
import dagger.Module
import dagger.Provides
import io.ktor.client.features.json.GsonSerializer

@Module
object CoreModule {

    @JvmStatic
    @Provides
    fun provideGsonBuilder(): GsonBuilder {
        val gsonBuilder = GsonBuilder()
        gsonBuilder(gsonBuilder)
        return gsonBuilder
    }

    @JvmStatic
    @Provides
    fun provideGson(gsonBuilder: GsonBuilder): Gson {
        return gsonBuilder.create()
    }

    @Provides
    fun provideGsonSerializer(): GsonSerializer {
        return GsonSerializer {
            gsonBuilder(this)
        }
    }

    @JvmStatic
    private fun gsonBuilder(gsonBuilder: GsonBuilder) {
        gsonBuilder.setPrettyPrinting()
        gsonBuilder.serializeNulls()
        gsonBuilder.disableHtmlEscaping()
        gsonBuilder.enableComplexMapKeySerialization()
        gsonBuilder.addSerializationExclusionStrategy(object : ExclusionStrategy {
            override fun shouldSkipField(f: FieldAttributes): Boolean {
                return f.getAnnotation(DoNotSerialize::class.java) != null
            }

            override fun shouldSkipClass(clazz: Class<*>): Boolean {
                return clazz.getAnnotation(DoNotSerialize::class.java) != null
            }
        })
        gsonBuilder.addDeserializationExclusionStrategy(object : ExclusionStrategy {
            override fun shouldSkipField(f: FieldAttributes): Boolean {
                return f.getAnnotation(DoNotDeserialize::class.java) != null
            }

            override fun shouldSkipClass(clazz: Class<*>): Boolean {
                return clazz.getAnnotation(DoNotDeserialize::class.java) != null
            }
        })
    }
}