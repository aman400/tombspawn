package com.tombspawn.data.cache.models

import com.tombspawn.base.common.CommonConstants

class ApkCache constructor(val params: Map<String, String>, val pathOnDisk: String? = null) {
    private val paramExcludeList = listOf(
        CommonConstants.COMMIT_AUTHOR,
        CommonConstants.COMMIT_ID,
        CommonConstants.COMMIT_MESSAGE
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ApkCache

        if (params.filterNot {
                it.key in paramExcludeList
            } != other.params.filterNot {
                it.key in paramExcludeList
            }) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = pathOnDisk.hashCode()
        result = 31 * result + params.hashCode()
        return result
    }
}