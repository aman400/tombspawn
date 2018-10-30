package com.ramukaka.utils

class Constants {
    class Common {
        companion object {
            const val HEADER_CONTENT_TYPE = "Content-type"
            const val VALUE_FORM_ENCODE = "application/x-www-form-urlencoded"

            const val APP_CONSUMER = "consumer"
            const val APP_FLEET = "fleet"

            const val OUTPUT_SEPARATOR = "##***##"
            const val ARG_OUTPUT_SEPARATOR = "OUTPUT_SEPARATOR"
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

            const val TYPE_SUBSCRIBE_CONSUMER = "subscribe-consumer"
            const val TYPE_SUBSCRIBE_FLEET = "subscribe-fleet"
            const val ATTACHMENT_TYPE_SELECT: String = "select"
            const val ATTACHMENT_TYPE_DEFAULT: String = "default"
            const val ACTION_CHOOSE_BRANCH: String = "choose_branch"
        }
    }

    class Github {
        companion object {
            const val HEADER_KEY_EVENT = "X-GitHub-Event"
            const val HEADER_VALUE_EVENT_PUSH = "push"
            const val HEADER_VALUE_EVENT_DELETE: String = "delete"
            const val HEADER_VALUE_EVENT_CREATE: String = "create"
        }
    }

    class Database {
        companion object {
            const val USER_TYPE_USER = "user"
            const val USER_TYPE_BOT = "bot"
            const val USER_TYPE_CHANNEL = "channel"
            const val USER_TYPE_GROUP = "group"
        }
    }
}