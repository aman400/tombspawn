package com.tombspawn.utils

class Constants {
    object Common {
        const val DEFAULT_HOST = "0.0.0.0"
        const val DEFAULT_PORT = 8662
        const val DEFAULT_SCHEME = "http"

        // Redis default config
        const val DEFAULT_REDIS_PORT = 6379
        const val DEFAULT_REDIS_HOST = "redis://127.0.0.1"

        const val APP_CONSUMER = "consumer"

        const val GET = "GET"
        const val PUT = "PUT"
        const val POST = "POST"
        const val DELETE = "DELETE"
        const val PATCH = "PATCH"
        const val HEAD = "HEAD"
        const val OPTIONS = "OPTIONS"
    }

    object Slack {
        const val CALLBACK_STANDUP_MESSAGE: String = "callback_standup_message"
        const val CALLBACK_STANDUP_DIALOG: String = "callback_standup_dialog"
        const val DEFAULT_BOT_ID: String = "USLACKBOT"

        const val CALLBACK_GENERATE_APK = "generate_apk_"
        const val CALLBACK_CONFIRM_GENERATE_APK = "confirm_generate_apk_"
        const val CALLBACK_CREATE_API = "create_api"

        const val TOKEN = "token"
        const val CHANNEL = "channel"
        const val TEXT = "text"
        const val TS = "ts"
        const val ATTACHMENTS = "attachments"
        const val DIALOG = "dialog"
        const val USER = "user"
        const val CURSOR = "cursor"
        const val LIMIT = "limit"

        const val CALLBACK_SUBSCRIBE_CONSUMER = "subscribe_consumer_"

        const val TYPE_SUBSCRIBE_CONSUMER = "subscribe-consumer"
        const val TYPE_SUBSCRIBE_FLEET = "subscribe-fleet"
        const val TRIGGER_ID: String = "trigger_id"
        const val CHANNEL_ID: String = "channel_id"

        const val TYPE_SELECT_VERB = "VERB"
        const val TYPE_SELECT_RESPONSE = "RESPONSE"
    }

    class Github {
        companion object {
            const val HEADER_KEY_EVENT = "X-Github-Event"
            const val HEADER_VALUE_EVENT_PUSH = "push"
            const val HEADER_VALUE_EVENT_DELETE: String = "delete"
            const val HEADER_VALUE_EVENT_CREATE: String = "create"
            const val HEADER_VALUE_EVENT_PING: String = "ping"
        }
    }

    class Database {
        companion object {
            const val USER_TYPE_USER = "user"
            const val USER_TYPE_BOT = "bot"
        }
    }

}