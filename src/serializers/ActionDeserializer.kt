package com.ramukaka.serializers

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.ramukaka.models.slack.ActionCallback
import com.ramukaka.models.slack.GenerateCallback
import com.ramukaka.utils.Constants
import java.lang.reflect.Type

object ActionDeserializer : JsonDeserializer<ActionCallback> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ActionCallback {
        json?.asJsonObject?.let {
            return when(it.get(Constants.Slack.TYPE).asString) {
                Constants.Slack.CALLBACK_CONFIRM_GENERATE_APK -> Gson().fromJson(it.toString(), GenerateCallback::class.java)
                else -> ActionCallback()
            }
        }
    }
}