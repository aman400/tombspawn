package com.ramukaka.models.slack

import com.google.gson.annotations.SerializedName

class Channel(@field:SerializedName("id") val id: String?,
              @field:SerializedName("name") val name: String?)