package com.tombspawn.models.config

import com.google.gson.annotations.SerializedName
import org.slf4j.LoggerFactory

data class Common constructor(@SerializedName("base_url") val baseUrl: String?,
                  @SerializedName("gradle_path") val gradlePath: String?,
                  @SerializedName("base_port") private val _basePort: Int? = 11110,
                  @SerializedName("parallel_threads") private val _parallelThreads: Int? = 2) {

    val basePort: Int
        get() {
            return if(_basePort in 1024..65535) {
                _basePort!!
            } else {
                LOGGER.warn("Base port must be in range from 1024 to 65535")
                DEFAULT_BASE_PORT
            }
        }

    val parallelThreads: Int
        get() {
            return if(_parallelThreads in 0..10) {
                _parallelThreads!!
            } else {
                DEFAULT_PARALLEL_THREADS
            }
        }


    companion object {
        private val LOGGER = LoggerFactory.getLogger("com.tombspawn.models.config.Common")
        const val DEFAULT_BASE_PORT = 11110
        const val DEFAULT_PARALLEL_THREADS = 2
    }
}