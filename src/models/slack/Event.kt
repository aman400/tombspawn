package models.slack

import com.google.gson.annotations.SerializedName

data class Event(

    @SerializedName("event_ts")
    val eventTimestamp: String? = null,

    @SerializedName("channel")
    val channel: String? = null,

    @SerializedName("text")
    val text: String? = null,

    @SerializedName("type")
    val type: EventType? = null,

    @SerializedName("user")
    val user: String? = null,

    @SerializedName("ts")
    val ts: String? = null
) {
    enum class EventType(val type: String) {
        @SerializedName("message")
        MESSAGE("message"),

        @SerializedName("user_change")
        USER_CHANGE("user_change"),

        @SerializedName("message.im")
        IM("message.im"),

        @SerializedName("team_join")
        TEAM_JOIN("team_join"),

        @SerializedName("url_verification")
        URL_VERIFICATION("url_verification"),

        @SerializedName("channel_archive")
        CHANNEL_ARCHIVED("channel_archive"),

        @SerializedName("channel_deleted")
        CHANNEL_DELETED("channel_deleted"),

        @SerializedName("event_callback")
        EVENT_CALLBACK("event_callback"),

        @SerializedName("app_rate_limited")
        RATE_LIMIT("app_rate_limited"),

        @SerializedName("app_mention")
        APP_MENTION("app_mention"),

        @SerializedName("interactive_message")
        INTERACTIVE_MESSAGE("interactive_message"),

        @SerializedName("message_action")
        MESSAGE_ACTION("message_action"),

        @SerializedName("dialog_submission")
        DIALOG_SUBMISSION("dialog_submission")
    }
}