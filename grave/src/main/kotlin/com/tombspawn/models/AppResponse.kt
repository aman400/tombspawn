package com.tombspawn.models

data class AppResponse constructor(
    val data: ByteArray?,
    val params: Map<String, String?>,
    val fileName: String?,
    val cached: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppResponse

        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false
        if (params != other.params) return false
        if (fileName != other.fileName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data?.contentHashCode() ?: 0
        result = 31 * result + params.hashCode()
        result = 31 * result + (fileName?.hashCode() ?: 0)
        return result
    }
}