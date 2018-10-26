package com.ramukaka.utils

class Constants {
    class Common {
        companion object {
            const val HEADER_CONTENT_TYPE = "Content-type"
            const val VALUE_FORM_ENCODE = "application/x-www-form-urlencoded"
        }
    }

    class Slack {
        companion object {

            const val SUBSCRIBE_GENERATE_APK = "subscribe_generate_apk"
            const val EVENT_TYPE_MESSAGE = "message"
            const val EVENT_TYPE_APP_MENTION = "app_mention"
            const val EVENT_TYPE_VERIFICATION = "url_verification"
            const val EVENT_TYPE_CALLBACK = "event_callback"
            const val EVENT_TYPE_RATE_LIMIT = "app_rate_limited"
            const val TOKEN = "token"
            const val CHANNEL = "channel"
            const val TEXT = "text"
            const val ATTACHMENTS = "attachments"
        }
    }

    class Github {
        companion object {
            const val HEADER_KEY_EVENT = "X-GitHub-Event"
            const val HEADER_VALUE_EVENT_PUSH = "push"
        }
    }
}