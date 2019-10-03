package com.tombspawn.models.slack


import com.google.gson.annotations.SerializedName

data class IMListData(
    @SerializedName("ims")
    val ims: List<IM>? = null,
    @SerializedName("ok")
    val ok: Boolean? = null,
    @SerializedName("response_metadata")
    val responseMetadata: ResponseMetadata? = null
) {
    data class ResponseMetadata(
        @SerializedName("next_cursor")
        val nextCursor: String?
    )

    data class IM(
        @SerializedName("created")
        val created: Int?,
        @SerializedName("id")
        val id: String?,
        @SerializedName("is_im")
        val isIm: Boolean?,
        @SerializedName("is_org_shared")
        val isOrgShared: Boolean?,
        @SerializedName("is_user_deleted")
        val isUserDeleted: Boolean?,
        @SerializedName("priority")
        val priority: Int?,
        @SerializedName("user")
        val user: String?
    )
}